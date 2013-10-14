package edu.urjc.pfc.rtsp.app;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kurento.commons.config.Parameters;
import com.kurento.commons.config.Value;
import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.kas.media.ports.MediaPort;
import com.kurento.kas.media.ports.MediaPortManager;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mediaspec.Transport;
import com.kurento.mediaspec.TransportRtp;
import com.kurento.mscontrol.commons.join.Joinable;
import com.kurento.mscontrol.kas.join.AudioJoinableStreamImpl_Multicast;
import com.kurento.mscontrol.kas.join.VideoJoinableStreamImpl_Multicast;
import com.kurento.mscontrol.kas.mediacomponent.MediaComponentAndroid;
import com.kurento.mscontrol.kas.mediacomponent.internal.AudioRecorderComponent;
import com.kurento.mscontrol.kas.mediacomponent.internal.VideoRecorderComponent;

import edu.urjc.pfc.rtsp.client.ClientRTSP;
import edu.urjc.pfc.rtsp.client.Info;
import edu.urjc.pfc.rtsp.app.R;

/**
 * 
 * @author laggc
 *
 */
public class PlayerActivity extends Activity implements Observer { 

	public final static String LOG_TAG = "PlayerActivity";

	private LinearLayout video_receive_surface_container;
	private MediaPort videoMediaPort = null;
	private MediaPort audioMediaPort = null;
	private VideoJoinableStreamImpl_Multicast joinVideoStream =  null;
	private AudioJoinableStreamImpl_Multicast joinAudioStream = null;
	private AudioRecorderComponent  arc = null;
	private VideoRecorderComponent vrc = null;

	private WakeLock wake;

	private Integer maxDelay_video;
	private Integer maxDelay_audio;
	private Integer port_teardown;

	private ProgressDialog mPd;
	
	private String title;

	private Handler tearDownHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			if(mPd.isShowing()) {
				mPd.dismiss();
			}
			
			

			Toast.makeText(PlayerActivity.this, "Emisión terminada.", Toast.LENGTH_SHORT).show();

			finish();
			return true;
		}
	});

	private Handler errorHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			if(mPd.isShowing()) {
				mPd.dismiss();
			}

			String msg_str;
			try {
				msg_str = msg.getData().getString("msg");
			}catch(Exception e) {
				msg_str = "Media no disponible";
			}

			Toast.makeText(PlayerActivity.this, msg_str, Toast.LENGTH_SHORT).show();
			finish();
			return true;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		Log.d(LOG_TAG, "OnCreate");

		checkPreferences();

		setViewById();

		Intent intent = getIntent();

		if(intent != null) {

			String uri_rtsp = intent.getDataString();

			if(uri_rtsp != null) {

				Log.d(LOG_TAG, uri_rtsp);
				title = uri2title(uri_rtsp);


				//Añado la actividad como Observador del cliente
				ClientRTSP.getInstance().deleteObservers();
				ClientRTSP.getInstance().addObserver(this);

				new ConnectionAsyncTask().execute(uri_rtsp);


			}else {
				Log.e(LOG_TAG, "La URI es null");
			}

		}else {
			Log.e(LOG_TAG, "El intent es null");
		}

		Parameters videoParams = new Parameters();
		videoParams.put(MediaComponentAndroid.VIEW_SURFACE_CONTAINER, new Value<ViewGroup>(video_receive_surface_container));

		Parameters audioParams = new Parameters();
		audioParams.put(MediaComponentAndroid.STREAM_TYPE, new Value<Integer>(android.media.AudioManager.STREAM_MUSIC));

		boolean syncMediaStreams = true;

		try {
			vrc = new VideoRecorderComponent(maxDelay_video,syncMediaStreams, videoParams);
			arc = new AudioRecorderComponent(maxDelay_audio, syncMediaStreams, audioParams);

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

			wake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
		} catch (Exception e) {
			Log.e(LOG_TAG,e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.player_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.player_menu_back:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.d(LOG_TAG, "OnResume");	
		wake.acquire();
	}

	@Override
	protected void onPause(){
		super.onPause();

		Log.d(LOG_TAG, "OnPause");	

		try { wake.release();}catch(Exception e) {}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.d(LOG_TAG, "OnDestroy");

		try { vrc.stop(); }catch(Exception e) {}
		try { arc.stop(); }catch(Exception e) {}
		try { joinVideoStream.stop();}catch(Exception e) {}
		try { joinAudioStream.stop();}catch(Exception e) {}
		try { MediaPortManager.releaseMediaPort(videoMediaPort); }catch(Exception e) {}
		try { MediaPortManager.releaseMediaPort(audioMediaPort); }catch(Exception e) {}

		try { vrc.flushAll(); }catch(Exception e) {}

		finish();
	}

	private void play(String sdp) {

		SessionSpec localSessionSpec;
		try {

			localSessionSpec = SdpConversor.sdp2SessionSpec(sdp);

			List<MediaSpec> medias = localSessionSpec.getMedias();

			if(medias == null || medias.isEmpty()) {
				Log.e(LOG_TAG,"No medias encontrados en el SDP");
				return;
			}

			if(medias.size()>2) {
				Log.e(LOG_TAG,"No se permite mas de 2 medias (VIDEO/AUDIO)");
				return;
			}

			Iterator<MediaSpec> iter = medias.iterator();

			while(iter.hasNext()) {

				MediaSpec mediaspec = iter.next();

				Set<MediaType> types = mediaspec.getType();

				Iterator<MediaType> iter2 = types.iterator();

				Transport transport = mediaspec.getTransport();
				TransportRtp  rtp = transport.getRtp();

				//Me subscribo a este grupo multicast para cuando finalice el media
				joinMulticast(rtp.getAddress(),port_teardown);


				while(iter2.hasNext()) {
					MediaType mediatype = iter2.next();

					if(mediatype.equals(MediaType.VIDEO) && (videoMediaPort == null) ) {
						//VIDEO
						videoMediaPort = MediaPortManager.takeMediaPort(rtp.getAddress(), rtp.getPort());

						joinVideoStream = new VideoJoinableStreamImpl_Multicast(localSessionSpec, videoMediaPort, maxDelay_video);

						vrc.join(Joinable.Direction.RECV, joinVideoStream);
						vrc.start();

					}else if (mediatype.equals(MediaType.AUDIO) && (audioMediaPort == null)){
						//AUDIO

						audioMediaPort = MediaPortManager.takeMediaPort(rtp.getAddress(), rtp.getPort());

						joinAudioStream = new AudioJoinableStreamImpl_Multicast(localSessionSpec, audioMediaPort, maxDelay_audio);

						arc.join(Joinable.Direction.RECV, joinAudioStream);
						arc.start();

					}else {
						//error
					}
					//Este break por si acaso hubiera mas MediaType
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void joinMulticast(final String address, final int port) {

		Log.d(LOG_TAG,"JOIN MULTICAST MC");
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					InetAddress group = InetAddress.getByName(address);
					MulticastSocket s = new MulticastSocket(port);
					s.joinGroup(group);
					byte[] buf = new byte[1000];
					DatagramPacket recv = new DatagramPacket(buf, buf.length);
					for(;;) {
						s.receive(recv);

						String str = new String(recv.getData(),0,recv.getLength(),"UTF-8");

						if(str.equals(title)) {
							Log.d(LOG_TAG, "RECIBIDO TEARDOWN");
							tearDownHandler.sendMessage(new Message());
							break;

						}else {
							continue;
						}
					}

				} catch (Exception e) {
					Log.e(LOG_TAG, e.getMessage());
				}

			}
		}).start();

	}

	// Inicializate the all views
	private void setViewById() {

		video_receive_surface_container = (LinearLayout) findViewById(R.id.video_receive_surface_container);
	}

	private void checkPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			maxDelay_video = Integer.valueOf(prefs.getString(getString(R.string.preference_delay_video_key), "4000"));
		}catch(Exception e) {
			maxDelay_video = 4000;
		}

		try {
			maxDelay_audio = Integer.valueOf(prefs.getString(getString(R.string.preference_delay_audio_key), "4000"));
		}catch(Exception e) {
			maxDelay_audio=4000;
		}

		try {
			port_teardown = Integer.valueOf(prefs.getString(getString(R.string.preference_port_teardown_key), "6000"));
		}catch(Exception e) {
			port_teardown=6000;
		}
	}

	@Override
	public void update(Observable o, Object arg) {

		if(mPd.isShowing()) {
			mPd.dismiss();
		}

		o.deleteObservers();
		Info p = (Info)arg;

		/*
			String sdp;

			sdp="v=0\n";
			sdp+="o=- 0 0 IN IP4 127.0.0.1\n";
			sdp+="s=SessionName\n";
			sdp+="c=IN IP4 224.0.0.100\n";
			sdp+="t=0 0\n";
			sdp+="m=video 5000 RTP/AVP 96\n";
			sdp+="a=control:video\n";
			sdp+="a=rtpmap:96 H264/90000\n";


			sdp+="m=audio 5002 RTP/AVP 0\n";
			sdp+="a=control:audio\n";
			sdp+="a=rtpmap:0 PCMU/8000\n";

		 */
		String sdp = p.getSdp();



		if( (p.isPlay()) && (sdp != null) ) {
			play(p.getSdp());
		}else {
			Log.d(LOG_TAG,p.getMsg());
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("msg", p.getMsg());
			msg.setData(data);
			errorHandler.sendMessage(msg);
		}
	}

	
	private String uri2title(String uri) {

		//Limpiamos la URI de la IP y el puerto
		try {
			uri = uri.trim();

			if(uri.startsWith("rtsp://")) {
				uri= uri.substring(7);
				String[] tokens1 = uri.split("/");

				int n = tokens1.length - 1;
				String title = tokens1[n];

				return title;	
			}
			else {
				throw new Exception();
			}

		}catch(Exception e) {
			Log.d(LOG_TAG, "La URI no tiene el formato esperado\n" + e.getMessage());
		}

		return null;
	}
	
	/**
	 * Clase privada que crea un AsynTask que realiza la conexion para el protocolo RTSP.
	 * @author laggc
	 *
	 */
	private class ConnectionAsyncTask extends AsyncTask<String, Integer, Void> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mPd = ProgressDialog.show(PlayerActivity.this, "PlayerActivity", "Obteniendo SDP",true, true, new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					ConnectionAsyncTask.this.cancel(true);
					finish();
				}
			});
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);


		}

		@Override
		protected Void doInBackground(String... uriRTSP) {

			//Consigo la URI que se ha introducido.
			String uriPlay = uriRTSP[0];

			Log.d(LOG_TAG,"PlayMedia: " + uriPlay);
			ClientRTSP.getInstance().playMedia(uriPlay);

			return null;
		}
	}
}
