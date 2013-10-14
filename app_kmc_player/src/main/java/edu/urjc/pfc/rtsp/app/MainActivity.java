/**
 * TODO:
 * 
 * Lo del SDP.
 *
 * esta mierda no actualiza
 * 
 * a veces casca el servidor de video y el web va bien...
 * 
 *	las preferencias por defecto lo del null y para si son int.
 *
 * lo de la webcam el audio y tal
 *
 * 
 * aspecto/ratio de la imagen comentado por Luis
 * 
 * onResume onPause etc etc
 * 
 * 
 * 
 * Cosas ya hechas:
 * -ClientePC
 */

package edu.urjc.pfc.rtsp.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.urjc.pfc.rtsp.app.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

/**
 * 
 * @author laggc
 *
 */
public class MainActivity extends Activity {

	public final static String LOG_TAG = "MainActivity";

	private String medias = null;
	private boolean wifiOn = false;

	private Timer timerPing;

	private Handler pingHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			//Conexion WIFI
			ConnectivityManager manager = (ConnectivityManager)getSystemService(MainActivity.CONNECTIVITY_SERVICE);
			setWifiOn(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting());

			//Servidor RTSP
			String medias = (String)msg.obj;
			setServerState(medias);

			return true;
		}
	});

	public String getMedias() {
		return medias;
	}

	public synchronized void setServerState(String medias) {

		boolean state = (medias!=null);

		if(state) {
			Log.d(LOG_TAG, "Server online");

			setIconServer(State.ONLINE);

			if(checkMedias(medias)>0){
				loadWebView();
			}else {
				Log.d(LOG_TAG, "No hay medias");
				loadMsgNoMedia();
			}

		}else {
			Log.d(LOG_TAG, "Server offline");
			setIconServer(State.OFFLINE);
			loadWelcome();	
		}
	}

	/**
	 * Esta función devuelve el número de medias en emisión por el servidor.
	 * @param medias
	 * @return
	 */
	private int checkMedias(String medias)  {

		try {
			Log.i(LOG_TAG,medias);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(medias));
			Document doc = db.parse(is);
			NodeList node_medias = doc.getElementsByTagName("media");
			
			return node_medias.getLength();
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			return 0;
		}
	}

	/**
	 * Carga texto de bienvenida
	 */
	private void loadWelcome() {

		WebView browser = (WebView) findViewById(R.id.webkit);
		ScrollView texto= (ScrollView)this.findViewById(R.id.scrollTextoBienvenida);
		TextView textViewNoMedia = (TextView) findViewById(R.id.textViewNoMedia);

		browser.clearView();

		texto.setVisibility(View.VISIBLE);
		browser.setVisibility(View.GONE);
		textViewNoMedia.setVisibility(View.GONE);
	}

	/**
	 * Carga la pagina con los medias del cliente
	 */
	private void loadWebView() {

		WebView browser = (WebView) findViewById(R.id.webkit);
		ScrollView texto= (ScrollView)this.findViewById(R.id.scrollTextoBienvenida);
		TextView textViewNoMedia = (TextView) findViewById(R.id.textViewNoMedia);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String ip_server = prefs.getString(getString(R.string.preference_ip_key), "");
		String port_http_server  = prefs.getString(getString(R.string.preference_port_http_key), "");
		String name_server  = prefs.getString(getString(R.string.preference_name_server_key), "");
		

		browser.setVerticalScrollBarEnabled(false);
		browser.setHorizontalScrollBarEnabled(false);
		String url = "http://"+ip_server+":"+port_http_server+"/"+ name_server +"/lightClient";
		browser.loadUrl(url);
		browser.reload();

		texto.setVisibility(View.GONE);
		browser.setVisibility(View.VISIBLE);
		textViewNoMedia.setVisibility(View.GONE);
	}
	
	private void loadMsgNoMedia() {
		
		WebView browser = (WebView) findViewById(R.id.webkit);
		ScrollView texto= (ScrollView)this.findViewById(R.id.scrollTextoBienvenida);
		TextView textViewNoMedia = (TextView) findViewById(R.id.textViewNoMedia);
		
		texto.setVisibility(View.GONE);
		browser.setVisibility(View.GONE);
		textViewNoMedia.setVisibility(View.VISIBLE);
	}

	private void setIconServer(State state) {

		ImageView iv_stateServer = (ImageView) findViewById(R.id.iv_stateServer);
		Resources res = getResources();
		Drawable imagenServer;

		if(state == State.ONLINE) {
			imagenServer = res.getDrawable(R.drawable.clouddark);
		}else {
			imagenServer = res.getDrawable(R.drawable.cloudlight);
		}
		
		iv_stateServer.setImageDrawable(imagenServer);
	}

	public boolean isWifiOn() {
		return wifiOn;
	}

	public void setWifiOn(boolean wifiOn) {
		this.wifiOn = wifiOn;

		ImageView iv_stateWIfi = (ImageView) findViewById(R.id.iv_stateWifi);

		Resources res = getResources();
		if(wifiOn) {
			Drawable imagenWifi = res.getDrawable(R.drawable.signaldark);
			iv_stateWIfi.setImageDrawable(imagenWifi);
		}else {
			Drawable imagenWifi = res.getDrawable(R.drawable.signallight);
			iv_stateWIfi.setImageDrawable(imagenWifi);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setViewById();

		checkPreferencias();
	}

	@Override        
	protected void onSaveInstanceState(Bundle SavedInstanceState) {
		super.onSaveInstanceState(SavedInstanceState);   


		//Guardo el valor de las variables para que al girar la pantalla salgan como deben.
		SavedInstanceState.putBoolean("wifi-state", wifiOn);
		SavedInstanceState.putString("medias", medias );
	}

	@Override    
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);  

		//Cargo el valor correcto.
		setServerState(savedInstanceState.getString("medias"));
		setWifiOn(savedInstanceState.getBoolean("wifi-state"));
	} 

	private void lanzarTaskPing() {

		//Creo un TimerTask para que realice ping periodicos
		TimerTaskPing timerTaskPing = new TimerTaskPing();
		timerPing = new Timer();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String timePing_str = prefs.getString(getString(R.string.preference_tiemposPing_key), null);


		int timePing = 5;
		try {
			timePing = Integer.valueOf(timePing_str);
		}catch(Exception e) {
			Toast.makeText(MainActivity.this, "Error cargando preferencia de tiempo de refresco.\nValor por defecto tomado.", Toast.LENGTH_SHORT).show();
		}

		timerPing.schedule(timerTaskPing, 0, timePing*1000);
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.i(LOG_TAG, "onResume");

		lanzarTaskPing();  

	}

	@Override
	protected void onPause(){
		super.onPause();

		Log.i(LOG_TAG, "onPause");

		try {
			if(timerPing != null) {
				timerPing.cancel();
			}
		}catch(Exception e) {}

	}

	@Override	
	protected void  onRestart() {
		super.onRestart();
		Log.i(LOG_TAG, "onRestart");
	}

	@Override
	protected void onStop(){
		super.onStop();
		Log.i(LOG_TAG, "onStop");
		try {
			if(timerPing != null) {
				timerPing.cancel();
			}
		}catch(Exception e) {}
	}

	/**
	 * Compruebo si tengo preferencias, y si no, carga unas por defecto
	 */
	private void checkPreferencias() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();

		String timePing_str = prefs.getString(getString(R.string.preference_tiemposPing_key), null);
		if(timePing_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para el tiempo de refresco con el servidor");
			editor.putString(getString(R.string.preference_tiemposPing_key), "5");
		}

		String ipServer_str = prefs.getString(getString(R.string.preference_ip_key), null);
		if(ipServer_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para la IP del servidor");
			editor.putString(getString(R.string.preference_ip_key), "193.168.1.100");
		}

		String portHTTP_str = prefs.getString(getString(R.string.preference_port_http_key), null);
		if(portHTTP_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para el puerto del servidor HTTP");
			editor.putString(getString(R.string.preference_port_http_key), "8080");
		}

		String portRTSP_str = prefs.getString(getString(R.string.preference_port_rtsp_key), null);
		if(portRTSP_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para el puerto del servidor RTSP");
			editor.putString(getString(R.string.preference_port_rtsp_key), "5454");
		}

		String delayVideo_str = prefs.getString(getString(R.string.preference_delay_video_key), null);
		if(delayVideo_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para delay máximo del video");
			editor.putString(getString(R.string.preference_delay_video_key), "4000");
		}

		String delayAudio_str = prefs.getString(getString(R.string.preference_delay_audio_key), null);
		if(delayAudio_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para delay máximo del audio");
			editor.putString(getString(R.string.preference_delay_audio_key), "4000");
		}

		String portTeardown_str = prefs.getString(getString(R.string.preference_port_teardown_key), null);
		if(portTeardown_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para puerto del TEARDOWN");
			editor.putString(getString(R.string.preference_port_teardown_key), "6000");
		}
		
		String nameServer_str = prefs.getString(getString(R.string.preference_name_server_key), null);
		if(nameServer_str==null) {
			Log.i(LOG_TAG, "Valor por defecto tomado para el nombre del server");
			editor.putString(getString(R.string.preference_name_server_key), "KMC-Server");
		}

		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_menu_ac_config:
			lanzarConfiguracion();
			return true;
		case R.id.main_menu_exit:
			lanzaSalida();
			return true;
		case R.id.main_menu_ac_wifi:
			lanzaWifiSettings();
			return true;
		case R.id.main_menu_develop:
			lanzaModoDesarrollo();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void lanzaModoDesarrollo() {
		Intent intent = new Intent(this, DevelopActivity.class);
		startActivity(intent);
	}

	private void lanzaWifiSettings() {
		Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		startActivity(intent);
	}

	private void setViewById() {

		setContentView(R.layout.activity_main);


		TextView textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setText("Bienvenido a KMC Player");

		TextView textView2 = (TextView) findViewById(R.id.textView2);
		textView2.setText("Esta aplicación le permitirá reproducir los flujos de video emitidos desde un servidor multicast.");

		TextView textView3 = (TextView) findViewById(R.id.textView3);
		SpannableStringBuilder ssb3 = new SpannableStringBuilder("Para empezar, debe conectarse a una red WIFI, en la cual haya un servidor RTSP corriendo. Puede acceder al sistema de configuración de WIFI de su teléfono pulsando   ");
		Bitmap image3 = BitmapFactory.decodeResource(getResources(),R.drawable.wifi_light_packet);
		ImageSpan imageSpan3 = new ImageSpan(this, image3);
		ssb3.setSpan(imageSpan3, ssb3.length()-1, ssb3.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textView3.setText(ssb3, BufferType.SPANNABLE);

		TextView textView4 = (TextView) findViewById(R.id.textView4);
		SpannableStringBuilder ssb4 = new SpannableStringBuilder("Una vez que se haya conectado a una red WIFI podra ver como en la barra superior este icono     está encendido");
		Bitmap image4 = BitmapFactory.decodeResource(getResources(),R.drawable.signal_light_packet);
		ImageSpan imageSpan4 = new ImageSpan(this, image4);
		ssb4.setSpan(imageSpan4, 93, 94, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textView4.setText(ssb4, BufferType.SPANNABLE);


		TextView textView5 = (TextView) findViewById(R.id.textView5);
		SpannableStringBuilder ssb5 = new SpannableStringBuilder("Una vez conectado a una red wifi, si el siguiente icono se enciende     se habrá establecido conexión con el servidor y en esta misma pantalla se mostrarán los medias disponibles.");
		Bitmap image5 = BitmapFactory.decodeResource(getResources(),R.drawable.cloud_light_packet);
		ImageSpan imageSpan5 = new ImageSpan(this, image5);
		ssb5.setSpan(imageSpan5, 69, 70, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textView5.setText(ssb5, BufferType.SPANNABLE);

		TextView textView6 = (TextView) findViewById(R.id.textView6);
		SpannableStringBuilder ssb6 = new SpannableStringBuilder("Si no consigue establecer la conexión con el servidor RTSP, compruebe la configuración pulsando     ");
		Bitmap image6 = BitmapFactory.decodeResource(getResources(),R.drawable.settings_light_packet);
		ImageSpan imageSpan6 = new ImageSpan(this, image6);
		ssb6.setSpan(imageSpan6, 97, 98, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textView6.setText(ssb6, BufferType.SPANNABLE);	
		
		
		TextView textViewNoMedia = (TextView) findViewById(R.id.textViewNoMedia);
		textViewNoMedia.setText("No se ha encontrado ningún video.");
		textViewNoMedia.setVisibility(View.GONE);
	}

	/**
	 * Realiza un ping al servidor.
	 * Esta funcion realiza una conexion de red y no puede ser lanzada desde 
	 * el hilo principal.
	 * Notifica mediante un handler el restultado del ping.
	 * Si se encuentra el servidor y este contesta true.
	 * En otro caso false.
	 */
	public void pingServidor() {

		Log.i(LOG_TAG, "PingServidor");

		try {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String ip_server_web = prefs.getString(getString(R.string.preference_ip_key), "");
			String port_server_web= prefs.getString(getString(R.string.preference_port_http_key), "");
			String name_server= prefs.getString(getString(R.string.preference_name_server_key), "");

			final String url_string = "http://" + ip_server_web.trim() + ":" + port_server_web.trim() + "/" + name_server +"/GetMedias";


			String aux = "";
			HttpURLConnection conn = null;
			try {
				URL url = new URL(url_string);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					aux += line;
				}
				rd.close();
			} catch (Exception e) {

			}finally{
				if(conn != null) {
					try { conn.disconnect(); } catch(Exception e) {}
				}
			}

			Message msg=new Message();
			aux = (aux.equals("")?null:aux);
			msg.obj=aux;
			pingHandler.sendMessage(msg);

		}catch(Exception e) {}
	}

	public void lanzarConfiguracion() {
		Intent intent = new Intent(this, ConfigActivity.class);
		startActivity(intent);
	}

	private void lanzaSalida() {
		new Intent(Intent.ACTION_MAIN); 
		finish();
	}

	private class TimerTaskPing extends TimerTask {

		@Override
		public void run() {

			pingServidor();
		}
	}	
}