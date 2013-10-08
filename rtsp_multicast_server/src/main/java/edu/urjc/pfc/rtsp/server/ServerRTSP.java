package edu.urjc.pfc.rtsp.server;

import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mobicents.rtsp.RtspServerStackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Clase encargada de la parte servidora de la aplicación.
 * Se encarga tanto del protocolo RTSP como de la emision de los medios.
 * @author laggc
 *
 */
public enum ServerRTSP {

	INSTANCE;

	//Atributos
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private RtspServerStackImpl serverStack;

	private String host;
	private int port;
	private ArrayList<Media> medias;
	private ArrayList<String> sessions;

	//Get & Set

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	/**
	 * Constructor de la case servidor
	 * @param host IP donde se ejecutará el servidor
	 * @param port puerto donde se esperaran solicitudes RTSP
	 */
	private ServerRTSP() {

		logger.debug("Constructor ServerRTSP");

		//Obtengo del fichero de configuración la interfaz para el servidor
		String inter = Configuration.INSTANCE.getNombreInterfazRed();


		//Obtengo del fichero de configuración el puerto para el servidor
		int puerto = Configuration.INSTANCE.getPuertoServidor();

		//Obtengo la IP de esa interfaz
		String ipServer = getLocalIpAddress(inter);


		setHost(ipServer);
		setPort(puerto);

		medias = new ArrayList<Media>();
		sessions = new ArrayList<String>();

		try {
			start();
		}
		catch(Exception e) {
			logger.error("Connection Failed: " + e.getMessage());
			System.exit(-1);
		}

	}

	/**
	 * Dada el nombre de una interfaz obtiene la IP asociada.
	 * @param nameInterface
	 * @return
	 */
	private String getLocalIpAddress(String nameInterface) {

		Enumeration<NetworkInterface> theIntfList =  null;
		List<InterfaceAddress> theAddrList = null;
		NetworkInterface theIntf = null;
		InetAddress theAddr = null; 

		try
		{
			//Consigo todas las interfaces
			theIntfList = NetworkInterface.getNetworkInterfaces();;

			while(theIntfList.hasMoreElements())
			{
				theIntf = theIntfList.nextElement();

				//Veo si es la intefaz que quiero usar para el servidor.
				if(!theIntf.getDisplayName().equals(nameInterface)) {
					continue;
				}

				//Consigo la lista de direcciones de esta intefaz
				theAddrList = theIntf.getInterfaceAddresses();

				for(InterfaceAddress intAddr : theAddrList)
				{
					theAddr = intAddr.getAddress();

					//Compruebo que esta en IPv4
					if(theAddr.getClass().equals(Inet4Address.class)) {
						logger.trace("IP Server:\t" + theAddr.getHostAddress());
						return theAddr.getHostAddress();
					}
				}
			}
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		logger.error("IP Server:\t" + "No encontrada una IP para la interfaz: " + nameInterface);
		return "127.0.0.1";
	}

	/**
	 * Elimina todos los recursos asociados.
	 * Realmente no elimina la instancia ya que con el enum 
	 * esto no se puede realizar
	 */
	public void destroyInstance() {

		try{
			INSTANCE.deleteAllMedia();
		}catch(Exception e){}

		try{
			INSTANCE.stop();
		}catch(Exception e){}
	}

	/**
	 * Inicia el servicio RTSP.
	 *  * @return true si la conexión se ha realizado correctamente
	 */
	public boolean start() {
		try {
			serverStack = new RtspServerStackImpl(getHost(), getPort());
			RtspServerListener listener = new RtspServerListener(this);

			serverStack.setRtspListener(listener);
			serverStack.start();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Detiene el servicio RTSP
	 */
	public void stop() {

		try {
			serverStack.stop();
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Añade a la lista de medios emitiendose uno nuevo
	 * @param m medio que pasa a estar en emisión.
	 */
	public void addMedia(Media m) {
		medias.add(m);
		m.Streaming();
	}

	/**
	 * Borrar de la lista de medios emitiendose el media que coincida
	 * con el titulo que se le pasa como parámetro.
	 * @param title titulo del media a eliminarse.
	 */
	public synchronized void deleteMedia(String title) {
		Media m;
		Iterator<Media> i = medias.iterator();


		while(i.hasNext()) {
			m=i.next();

			if(m.getTitle().equals(title)) {
				m.StopStreaming();
				i.remove();
			}
		}

	}

	/**
	 * Borrar de la lista de medios emitiendose el media que coincida
	 * con el titulo que se le pasa como parámetro.
	 * @param title titulo del media a eliminarse.
	 */
	private synchronized void deleteAllMedia() {
		Media m;
		Iterator<Media> i = medias.iterator();


		while(i.hasNext()) {
			m=i.next();
			m.StopStreaming();
			i.remove();
		}
	}

	/**
	 * Dado un titulo devuelve el media si existe
	 * @param title titulo del media que se quiere obtener.
	 * @return el media que coincida en titulo, o null en otro caso
	 */
	public Media getMedia(String title) {

		Media m;
		Iterator<Media> i = medias.iterator();


		while(i.hasNext()) {
			m=i.next();

			if(m.getTitle().equals(title)) {
				return m;
			}
		}

		return null;

	}

	/**
	 * Añade a la lista de sesiones la que se le pasa por parámetro
	 * @param session sesion a añadir a la lista.
	 */
	public void addSession(String session) {
		sessions.add(session);
	}


	/**
	 * Devuelve un String en formato XML con los medias que se están
	 * reproduciendo en ese preciso instante en el servidor.
	 * @return
	 */
	public String getXMLmedias() {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation implementation = builder.getDOMImplementation();

			Document document = implementation.createDocument(null, "xml", null);
			document.setXmlVersion("1.0");

			//Elemento raiz
			Element raiz = document.createElement("medias");  

			//Introducimos la raiz
			document.getDocumentElement().appendChild(raiz); 


			String url = "rtsp://"+host+":"+port+"/";
			Media m;
			Iterator<Media> i = medias.iterator();

			while(i.hasNext()) {
				m=i.next();
				//Creamos un nuevo elemento
				Element elemento = document.createElement("media");

				//Introducimos la URL
				String url_aux = url+m.getTitle();
				Text text = document.createTextNode(url_aux); 

				raiz.appendChild(elemento);
				elemento.appendChild(text); 
			}

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			String output = writer.getBuffer().toString().replaceAll("\n|\r", "");

			return output;

		}catch(Exception e){
			return "";
		}
	}

	/**
	 * Comprueba si la sesión que se le pasa como parámetro esta ya en la lista
	 * @param session sesion que se quiere comprobar
	 * @return true si existe, false en otro caso
	 */
	public boolean checkSession(String session) {
		String s;
		Iterator<String> i = sessions.iterator();


		while(i.hasNext()) {
			s=i.next();

			if(s.equals(session)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Retorna toda la lista de medias en ejecución
	 * @return
	 */
	public ArrayList<Media> getMedias() {
		return medias;
	}

}
