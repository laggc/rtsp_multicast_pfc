package edu.urjc.pfc.rtsp.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.gstreamer.ClockTime;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase que crea la abstracción de un archivo para hacer streaming.
 * Contiene los métodos necesarios para realizar el streaming por si mismo,
 * usando la libreria GStreamer de Java.
 * @author laggc
 *
 */
public class Media {

	private static Logger logger = LoggerFactory.getLogger(Media.class);
	
	
	private String title;
	private Formats format;
	private String path;
	private String pathImage;
	private String ipMC;
	private long duration;
	private String sdp;

	private int portStream0;
	private int portStream1;
	private String stream0;
	private String stream1;

	private Thread ref_thread;
	private Pipeline ref_pipeline;

	private int portTeardown = 6000;

	private final int config_interval = 1;
	private final int mtu = 15000;
	private final int bitRateEncoding = 256;
	private final int refEncoding  = 12;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Formats getFormat() {
		return format;
	}

	public void setFormat(Formats format) {
		this.format = format;
	}

	private String getPath() {
		return path;
	}

	private void setPath(String path) {
		this.path = path;
	}

	public String getPathImage() {
		return pathImage;
	}

	public void setPathImage(String pathImage) {
		this.pathImage = pathImage;
	}

	public String getIpMC() {
		return ipMC;
	}

	private void setIpMC(String ipMC) {
		this.ipMC = ipMC;
	}

	public int getPortVideo() {
		return portStream0;
	}

	private void setPortVideo(int portVideo) {
		this.portStream0 = portVideo;
	}

	public int getPortAudio() {
		return portStream1;
	}

	private void setPortAudio(int portAudio) {
		this.portStream1 = portAudio;
	}

	private long getDuration() {
		return duration;
	}

	private void setDuration(long duration) {
		this.duration = duration;
	}

	public String getSdp() {
		return sdp;
	}

	private void setSdp(String sdp) {
		this.sdp = sdp;
	}

	private String getStream0() {
		return stream0;
	}

	private void setStream0(String stream0) {
		this.stream0 = stream0;
	}

	private String getStream1() {
		return stream1;
	}

	private void setStream1(String stream1) {
		this.stream1 = stream1;
	}

	public Media(String title_, Formats format_, String path_, String pathImage_, String ipMC_, int portVideo_, int portAudio_) {

		setTitle(title_);
		setFormat(format_);

		setPathImage(pathImage_);

		setIpMC(ipMC_);
		setPortVideo(portVideo_);
		setPortAudio(portAudio_);

		setStream0("streamid=0");
		setStream1("streamid=1");

		if(format == Formats.H264_Encoding_Video_WebCam || 
				format == Formats.H264_Encoding_Video || 
				format == Formats.H264_Video) {

			setPortAudio(-1);
			setStream1(null);
		}



		if(format_==Formats.H264_Encoding_Video_WebCam) {
			setPath(null);
			setDuration(-1);
		}else {
			setPath(path_);
			setDuration(60000);
			setDuration(CalculateDuration());
			

		}
		setSdp(GenerateSDP());
	}

	/**
	 * Genera un SDP genérico.
	 * @return
	 */
	private String GenerateSDP() {
		String sdp;

		sdp="v=0\n";
		sdp+="o=- 1188340656180883 1 IN IP4 " + getIpMC() + "\n";
		sdp+="s="+getTitle()+"\n";
		sdp+="c=IN IP4 "+getIpMC()+"\n";
		sdp+="t=0 0\n";
		sdp+="m=video "+getPortVideo()+" RTP/AVP 96\n";
		sdp+="a=control:"+getStream0()+"\n";
		sdp+="a=rtpmap:96 H264/90000\n";

		if(format == Formats.H264_Encoding_Video_Audio || format == Formats.H264_Video_Audio) {
			sdp+="m=audio "+getPortAudio()+" RTP/AVP 0\n";
			sdp+="a=control:"+getStream1()+"\n";
			sdp+="a=rtpmap:0 PCMU/8000\n";
		}

		return sdp;
	}

	public String generateTransport(String stream) {
		
		if(stream.equals(getStream0())) {
			return "RTP/AVP;multicast;destination="+getIpMC()+";port="+getPortVideo()+"-"+(getPortVideo()+1)+";ttl=16";
		}

		if(stream.equals(getStream1())) {
			return "RTP/AVP;multicast;destination="+getIpMC()+";port="+getPortAudio()+"-"+(getPortAudio()+1)+";ttl=16";
		}
		return null;
	}

	
	/**
	 * Calcula la duración del media.
	 * @return
	 */
	private long CalculateDuration() {

		long duration;

		Gst.init("GStreamer", new String[0]);

		final Pipeline pipe = Pipeline.launch("filesrc name=filesrc ! qtdemux name=demux demux.video_00 ! queue2 ! h264parse  ! rtph264pay name=rtph264pay ! udpsink name=udpsinkVideo");
		ref_pipeline = pipe;
		
		pipe.getElementByName("filesrc").set("location",getPath());
		pipe.getElementByName("rtph264pay").set("config-interval",config_interval);
		pipe.getElementByName("rtph264pay").set("mtu",mtu);
		pipe.getElementByName("udpsinkVideo").set("host", getIpMC());
		pipe.getElementByName("udpsinkVideo").set("port", getPortVideo());
		pipe.getElementByName("udpsinkVideo").set("sync", "true");
		pipe.getElementByName("udpsinkVideo").set("async", "true");

		
		final Thread t = new Thread(
				new Runnable()
				{
					public void run()
					{ 
						pipe.setState(State.PLAYING);
						Gst.main();     
						pipe.setState(State.NULL);
					}
				});
		t.start();
	
	
		while(pipe.getState()!=State.PAUSED){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			pipe.setState(State.PAUSED);
		}

		ClockTime time= pipe.queryDuration();
		duration = time.toMillis();

		t.interrupt();

		logger.info("Calculated duration: " + time.getMinutes() + ":"+time.getSeconds());

		return duration;
	}
	
	
	
	/**
	 * Emitirá video+audio codificando a H264
	 */
	private void StreamingEncodingH264VideoAudio() {

		Gst.init("GStreamer", new String[0]);

		//Creamos el pipe de GStreamer
		final Pipeline pipe = Pipeline.launch("filesrc name=filesrc ! decodebin name=dec dec. ! queue2 ! x264enc name=x264enc  ! rtph264pay name=rtph264pay ! udpsink name=udpsinkVideo dec. ! queue2 ! audioresample ! audioconvert ! mulawenc ! rtppcmupay  ! udpsink name=udpsinkAudio");
		ref_pipeline = pipe;
		
		//Introducimos los parámetros para los elementos del pipe
		pipe.getElementByName("filesrc").set("location",getPath());
		pipe.getElementByName("x264enc").set("ref",refEncoding);
		pipe.getElementByName("x264enc").set("bitrate",bitRateEncoding);
		pipe.getElementByName("rtph264pay").set("config-interval",config_interval);
		pipe.getElementByName("rtph264pay").set("mtu",mtu);
		pipe.getElementByName("udpsinkVideo").set("host", getIpMC());
		pipe.getElementByName("udpsinkVideo").set("port", getPortVideo());
		pipe.getElementByName("udpsinkVideo").set("sync", "true");
		pipe.getElementByName("udpsinkVideo").set("async", "true");
		pipe.getElementByName("udpsinkAudio").set("host", getIpMC());
		pipe.getElementByName("udpsinkAudio").set("port", getPortAudio());
		pipe.getElementByName("udpsinkAudio").set("sync", "true");
		pipe.getElementByName("udpsinkAudio").set("async", "true");
		

		/*Creamos un thread para que reproduzca el video y el hilo principal,
		 * se quedará esperando para poder liberar el pipe de GStreamer.
		 * La ejecución no se corta porque anteriormente (Streaming()) habia creado 
		 * ya un hilo para el Streaming
		 */
		final Thread t = new Thread(
				new Runnable()
				{
					public void run()
					{ 
						pipe.setState(State.PLAYING);
						Gst.main();     
						pipe.setState(State.NULL);
					}
				});
		t.start();
		ref_thread=t;


		/*El hilo principal se duerme durante el tiempo que dura el video, y luego
		 * libera sus recursos */
		try {
			logger.info("START PLAYING");
			Thread.sleep(getDuration());
		} catch (InterruptedException e) {
			logger.info("Interrupted exception:");
			e.printStackTrace();
		}

		logger.info("Reproducción terminada. Borrando streaming:" + getTitle());
		sendTeardown();
		ServerRTSP.INSTANCE.deleteMedia(getTitle());
	}

	/**
	 * Emitirá video codificando a H264
	 */
	private void StreamingEncodingH264Video() {

		Gst.init("GStreamer", new String[0]);

		final Pipeline pipe = Pipeline.launch("filesrc name=filesrc ! decodebin name=dec dec. ! queue2 ! x264enc name=x264enc  ! rtph264pay name=rtph264pay ! udpsink name=udpsinkVideo");
		ref_pipeline = pipe;
		
		pipe.getElementByName("filesrc").set("location",getPath());
		pipe.getElementByName("x264enc").set("ref",refEncoding);
		pipe.getElementByName("x264enc").set("bitrate",bitRateEncoding);
		pipe.getElementByName("rtph264pay").set("config-interval",config_interval);
		pipe.getElementByName("rtph264pay").set("mtu",mtu);
		pipe.getElementByName("udpsinkVideo").set("host", getIpMC());
		pipe.getElementByName("udpsinkVideo").set("port", getPortVideo());
		pipe.getElementByName("udpsinkVideo").set("sync", "true");
		pipe.getElementByName("udpsinkVideo").set("async", "true");


		final Thread t = new Thread(
				new Runnable()
				{
					public void run()
					{ 
						pipe.setState(State.PLAYING);
						Gst.main();     
						pipe.setState(State.NULL);
					}
				});

		t.start();
		ref_thread=t;

		/*El hilo principal se duerme durante el tiempo que dura el video, y luego
		 * libera sus recursos */
		try {
			logger.info("START PLAYING");
			Thread.sleep(getDuration());
		} catch (InterruptedException e) {
			logger.info("Interrupted exception:");
			e.printStackTrace();
		}

		logger.info("Reproducción terminada. Borrando streaming:" + getTitle());
		sendTeardown();
		ServerRTSP.INSTANCE.deleteMedia(getTitle());
	}

	/**
	 * Emitirá video+audio directamente de un archivo previamente
	 * codificado en H264
	 */
	private void StreamingH264Video() {
		Gst.init("GStreamer", new String[0]);

		final Pipeline pipe = Pipeline.launch("filesrc name=filesrc ! qtdemux name=demux demux.video_00 ! queue2 ! h264parse  ! rtph264pay name=rtph264pay ! udpsink name=udpsinkVideo");
		ref_pipeline = pipe;
		
		pipe.getElementByName("filesrc").set("location",getPath());
		pipe.getElementByName("rtph264pay").set("config-interval",config_interval);
		pipe.getElementByName("rtph264pay").set("mtu",mtu);
		pipe.getElementByName("udpsinkVideo").set("host", getIpMC());
		pipe.getElementByName("udpsinkVideo").set("port", getPortVideo());
		pipe.getElementByName("udpsinkVideo").set("sync", "true");
		pipe.getElementByName("udpsinkVideo").set("async", "true");

		
		final Thread t = new Thread(
				new Runnable()
				{
					public void run()
					{ 
						pipe.setState(State.PLAYING);
						Gst.main();     
						pipe.setState(State.NULL);
					}
				});
		t.start();
		ref_thread=t;
		

		/*El hilo principal se duerme durante el tiempo que dura el video, y luego
		 * libera sus recursos */
		try {
			logger.info("START PLAYING");
			Thread.sleep(getDuration());
		} catch (InterruptedException e) {
			logger.info("Interrupted exception:");
			e.printStackTrace();
		}

		logger.info("Reproducción terminada. Borrando streaming:" + getTitle());
		sendTeardown();
		ServerRTSP.INSTANCE.deleteMedia(getTitle());

	}

	/**
	 * Emitirá video directamente de un archivo previamente
	 * codificado en H264
	 */
	private void StreamingH264VideoAudio() {
		Gst.init("GStreamer", new String[0]);
		
		final Pipeline pipe = Pipeline.launch("filesrc name=filesrc ! qtdemux name=demux demux.video_00 ! queue2 ! h264parse  ! rtph264pay name=rtph264pay ! udpsink name=udpsinkVideo demux.audio_00 ! decodebin2 ! queue2 ! audioresample ! audioconvert ! mulawenc ! rtppcmupay  ! udpsink name=udpsinkAudio");
		ref_pipeline = pipe;
		
		pipe.getElementByName("filesrc").set("location",getPath());
		pipe.getElementByName("rtph264pay").set("config-interval",config_interval);
		pipe.getElementByName("rtph264pay").set("mtu",mtu);
		pipe.getElementByName("udpsinkVideo").set("host", getIpMC());
		pipe.getElementByName("udpsinkVideo").set("port", getPortVideo());
		pipe.getElementByName("udpsinkAudio").set("host", getIpMC());
		pipe.getElementByName("udpsinkAudio").set("port", getPortAudio());
		 
		final Thread t = new Thread(
				new Runnable()
				{
					public void run()
					{ 
						pipe.setState(State.PLAYING);
						Gst.main();     
						pipe.setState(State.NULL);
					}
				});
		t.start();
		ref_thread=t;

		/*El hilo principal se duerme durante el tiempo que dura el video, y luego
		 * libera sus recursos */
		try {
			logger.info("START PLAYING");
			Thread.sleep(getDuration());
			
		} catch (InterruptedException e) {
			logger.info("Interrupted exception:");
			e.printStackTrace();
		}

		logger.info("Reproducción terminada. Borrando streaming:" + getTitle());
		sendTeardown();
		ServerRTSP.INSTANCE.deleteMedia(getTitle());

	}

	/**
	 * Emitirá el vide proveniente de la webcam codificandolo en H264
	 */
	private void StreamingEncodingH264WebCam() {

		Gst.init("GStreamer", new String[0]);

		final Pipeline pipe = Pipeline.launch("v4l2src name=v4l2src  ! textoverlay name=textoverlay ! x264enc name=x264enc  ! rtph264pay name=rtph264pay ! udpsink name=udpsinkVideo");
		ref_pipeline = pipe;

		pipe.getElementByName("v4l2src").set("device","/dev/video0");
		pipe.getElementByName("textoverlay").set("text","EMISIÓN DESDE KMC SERVER");
		
		pipe.getElementByName("x264enc").set("ref",refEncoding);
		pipe.getElementByName("x264enc").set("bitrate",bitRateEncoding);
		pipe.getElementByName("rtph264pay").set("config-interval",config_interval);
		pipe.getElementByName("rtph264pay").set("mtu",mtu);
		pipe.getElementByName("udpsinkVideo").set("host", getIpMC());
		pipe.getElementByName("udpsinkVideo").set("port", getPortVideo());
		pipe.getElementByName("udpsinkVideo").set("sync", "false");
		pipe.getElementByName("udpsinkVideo").set("async", "false");


		final Thread t = new Thread(
				new Runnable()
				{
					public void run()
					{ 
						pipe.setState(State.PLAYING);
						Gst.main();     
						pipe.setState(State.NULL);
					}
				});
		t.start();
		ref_thread=t;
		
		
	}

	/**
	 * Inicia el streaming en la dirección y puerto especificados
	 */
	public void Streaming() {

		//Creo un nuevo hilo para reproducir el video.
		final Thread t = new Thread(

				new Runnable()
				{
					public void run()
					{ 
						switch(getFormat()) {
						case H264_Encoding_Video:
							StreamingEncodingH264Video();
							break;
						case H264_Encoding_Video_Audio:
							StreamingEncodingH264VideoAudio();
							break;
						case H264_Video_Audio:
							StreamingH264VideoAudio();
							break;
						case H264_Video:
							StreamingH264Video();
							break;
						case H264_Encoding_Video_WebCam:
							StreamingEncodingH264WebCam();
							break;
						}
					}
				});
		
		t.start();
		
	}

	/**
	 * Para el streaming
	 */
	public void StopStreaming() {
		try {
			sendTeardown();
			ref_pipeline.setState(State.NULL);
			ref_thread.interrupt();
		}catch(Exception e){}

	}

	public void sendTeardown() {
		logger.info("Send Teardown");

		try {
			String msg = title;
			InetAddress group = InetAddress.getByName(ipMC);
			MulticastSocket s = new MulticastSocket();
			s.joinGroup(group);
			DatagramPacket dtp = new DatagramPacket(msg.getBytes(), msg.length(), group, portTeardown);

			s.send(dtp);
		}catch(Exception e) {}
	}


}