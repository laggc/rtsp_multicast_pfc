package edu.urjc.pfc.rtsp.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.gstreamer.Gst;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;
import org.gstreamer.swing.VideoComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.urjc.pfc.rtsp.client.ClientRTSP;
import edu.urjc.pfc.rtsp.client.Info;


public class SimpleClient implements Observer {

	private static Logger logger = LoggerFactory.getLogger(SimpleClient.class);


	public SimpleClient(String url) {

		//Añado la GUI como observador del ClientRTSP
		ClientRTSP.getInstance().addObserver(this);

		ClientRTSP.getInstance().playMedia(url);

	}

	@Override
	public void update(Observable o, Object arg) {

		try {
			Info p = (Info)arg;

			String sdp = p.getSdp();

			if( (p.isPlay()) && (sdp != null) ) {
				logger.info("SDP RECIBIDO:\n" + sdp);

				URI uriSDP = creaFicheroSdp(sdp);

				PlaySdp(uriSDP);
			}else {
				logger.error("No se ha recibido el SDP.");
			}

		}catch(Exception e) {
			logger.error("Error no controlado:" + e.getMessage());
		}
	}

	/**
	 * Para reproducir a partir de un SDP Gstreamer está esperando un fichero *.sdp
	 * que contenga el mismo. Esta función creará el fichero y proporcionará la URI
	 * donde se encuentre.
	 * @param sdp
	 * @return
	 */
	private URI creaFicheroSdp(String sdp) {
		FileWriter fichero = null;
		PrintWriter pw = null;
		try
		{
			String fileName = "temp.sdp";

			fichero = new FileWriter(fileName);
			pw = new PrintWriter(fichero);

			pw.println(sdp);
			
			pw.close();
			
			URI uriSDP = new URI("file://" + new File(".").getCanonicalPath() + "/" + fileName);
			
			logger.info(uriSDP.getPath());

			return uriSDP;

		} catch (Exception e) {
			logger.error("Error creando el fichero que contendrá el SDP.");
			return null;
		} finally {
			try {
				if (fichero != null) {
					fichero.close();
				}
			} catch (Exception e2) {}
		}
	}


	/**
	 * Toma como argumento una URI que indica el path del SessionDescription que se quiere reproducir.
	 * Ejemplo: URI uriSDP = new URI("file:///home/laggc/Escritorio/borrar.sdp");
	 * Se creará un JFrame en el cual se reproducirá el medio.
	 * @return 
	 * @partam uriSDP
	 * URI del SDP que se quiere reproducir.
	 */
	public static void PlaySdp(URI uriSDP)
	{
		Gst.init("PLAY SDP", new String[0]);

		final PlayBin2 playbin = new PlayBin2("PlaySDP");

		playbin.setURI(uriSDP);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				VideoComponent videoComponent = new VideoComponent();
				playbin.setVideoSink(videoComponent.getElement());

				JFrame frame = new JFrame("Player");
				frame.getContentPane().add(videoComponent, BorderLayout.CENTER);
				frame.setPreferredSize(new Dimension(640, 480));
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
				playbin.setState(State.PLAYING);       
			}
		});

		Gst.main();
		playbin.setState(State.NULL);	
	}

	/**
	 * Intentará buscar un archivo de configuración para log4j.
	 * Si no lo encuentra cargará su configuración básica.
	 */
	private static void Config_log4j() {

		try
		{
			Properties props = new Properties();
			props.load(new FileInputStream("log4j.properties"));
			PropertyConfigurator.configure(props);
		}
		catch(Exception e){}

		BasicConfigurator.configure();	
		logger.info("No se ha encontrado el fichero log4j.properties.");
		logger.info("Cargada configuracion basica de log4j.");
	}



	public static void main(String[] args) {

		Config_log4j();

		if(args != null && args.length>0 && args[0] != null) {
			logger.info(args[0]);
			new SimpleClient(args[0]);

		}else {
			logger.error("Introduzca el recurso a reproducir como parámetro.\nEjemplo: rtsp://193.168.1.109:5454/media");
		}
	}
}
