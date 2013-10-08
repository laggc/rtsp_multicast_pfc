package edu.urjc.pfc.rtsp.client;


import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.sdp.MediaDescription;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.jboss.netty.util.CharsetUtil;
import org.mobicents.rtsp.DefaultRtspRequest;
import org.mobicents.rtsp.RtspClientStackImpl;
import org.mobicents.rtsp.RtspRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Esta clase implementa el cliente RTSP.
 * Extienda la clase Observable, ya que notificara cuando ya se puede empezar a reproducir.
 * Implementa la clase Observer para poder observar a RtspClientStackImpl y ver si surje 
 * algún error.
 * @author laggc
 *
 */
public class ClientRTSP extends Observable implements Observer{  

	private Logger logger = LoggerFactory.getLogger(ClientRTSP.class);

	private static ClientRTSP INSTANCE = null;

	private RtspClientStackImpl clientStack;
	private HttpMethod cmd_sent;
	private URI media;
	private int	cSeq;
	private String session=null;
	private String sdp;
	private ArrayList<String> streamsSetup;


	/**
	 * Constructor privado, ya que esta clase es singleton.
	 */
	private ClientRTSP() {

	}

	private synchronized static void createInstance() {
		if (INSTANCE == null) { 

			INSTANCE = new ClientRTSP();
		}
	}

	public static ClientRTSP getInstance() {
		createInstance();
		return INSTANCE;
	}

	//Gets & Sets

	public void setCmd_sent(HttpMethod setup) {
		this.cmd_sent = setup;
	}

	public HttpMethod getCmd_sent() {
		return cmd_sent;
	}

	public void setMedia(URI media) {
		this.media = media;
	}

	public URI getMedia() {
		return media;
	}

	public void setcSeq(int cSeq) {
		this.cSeq = cSeq;
	}

	public int getcSeq() {
		return cSeq;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getSession() {
		return session;
	}

	public String getSdp() {
		return sdp;
	}

	public void setSdp(String sdp) {
		this.sdp = sdp;
	}

	/**
	 * Intentará establecer una conexión con un servidor que
	 * se encuentre en la IP y Puerto que se le pasa por parametros.
	 * @param ip ip del servidor
	 * @param port puerto del servidor
	 */
	public void iniciaCliente(String ip, int port) {

		paraCliente();

		try {
			clientStack = new RtspClientStackImpl(ip,port);

			clientStack.addObserver(this);

			RtspClientListener listener = new RtspClientListener(this);
			clientStack.setRtspListener(listener);

			clientStack.start();

		} catch (Exception e) {
			logger.error("Error start the client.");
			createNotify("Error start the client.", false);
		}
	}

	/**
	 * Detiene la conexión con el servidro en el caso de que 
	 * se haya realizado.
	 */
	public void paraCliente() {

		try {
			clientStack.stop();

		} catch (Exception e) {
			logger.error("Error stop the client.");
		}
	}

	/**
	 * Envía al servidor una petición de DESCRIBE
	 */
	public void sendDescribe() {

		try {
			RtspRequest describe = new DefaultRtspRequest(RtspMethods.DESCRIBE, getMedia().toASCIIString());
			describe.setHeader("CSeq", getcSeq());
			setCmd_sent(RtspMethods.DESCRIBE);

			sdp=null;

			clientStack.sendRquest(describe);

		} catch (Exception e) {
			logger.error("ERROR DESCRIBE: " + e.getMessage());
			createNotify("ERROR DESCRIBE: " + e.getMessage(), false);
		}
	}

	/**
	 * Envía al servidor una peticón de SETUP del stream que se le pasa por URL
	 * @param url URL del stream que se quiere pedir
	 */
	public void sendSetup(String url) {

		try {

			RtspRequest setup = new DefaultRtspRequest(RtspMethods.SETUP,url);
			setup.setHeader("CSeq", getcSeq());
			setup.setHeader("Transport", "RTP/AVP/UDP;multicast");

			if(session != null) {
				setup.setHeader("Session", session);
			}

			setCmd_sent(RtspMethods.SETUP);

			clientStack.sendRquest(setup);

		} catch (Exception e) {

			logger.error("ERROR SETUP\t" + e.getMessage() + "\n");
			createNotify("ERROR SETUP\t" + e.getMessage(), false);
		}
	}

	/**
	 * Envía al servidor una peticón de PLAY del stream que se le pasa por URL
	 * @param url URL del stream que se quiere pedir
	 */
	public void sendPlay(String url) {

		try {

			RtspRequest play = new DefaultRtspRequest(RtspMethods.PLAY, url);
			play.setHeader("CSeq", getcSeq());
			play.setHeader("Session", getSession());
			setCmd_sent(RtspMethods.PLAY);


			clientStack.sendRquest(play);

		} catch (Exception e) {
			logger.error("ERROR PLAY" + e.getMessage());
			createNotify("ERROR PLAY" + e.getMessage(),false);
		}
	}

	/**
	 * Se ejecutará cuando se reciba un response despues de enviar un DESCRIBE
	 * Comprobará que todo es correcto, y si es así enviará una petición de SETUP
	 * @param response
	 */
	public void describeResponse(HttpResponse response) {
		try {
			checkCseq(response);

			ChannelBuffer content = response.getContent();

			checkSDP(response);

			parseSDP(content);

			//Ahora enviamos el setup del primer stream y lo sacamos de la lista
			if(!streamsSetup.isEmpty()) {
				String stream = streamsSetup.get(0);
				streamsSetup.remove(0);

				sendSetup(getMedia() + "/" + stream);
			}

		} catch (Exception e) {
			createNotify("ERROR DESCRIBE RESPONSE: " + e.getMessage(),false);
		}
	}

	/**
	 * Se ejecutará cuando se reciba un response despues de enviar un SETUP
	 * Comprobará que todo es correcto, y si es así enviará una petición de SETUP
	 * si es que quedan mas stream por los que enviar SETUP, o enviará una petición
	 * de PLAY si era el último/único stream de SETUP que se esperaba.
	 * @param response
	 */
	public void setupResponse(HttpResponse response) {
		logger.trace("RESPUESTA SETUP RECIBIDA");

		if(!checkSession(response)){
			logger.error("error session");
			return;
		}

		try {
			checkCseq(response);

			/* Cuando recibimos una respuesta a un setup, pueden quedar aún 
			 * medias por enviar su setup.
			 */

			//Si aun quedan setup por enviar
			if(!streamsSetup.isEmpty()) {
				String stream = streamsSetup.get(0);
				streamsSetup.remove(0);

				sendSetup(getMedia() + "/" + stream);
			}
			else  {
				//Si ya se han configurado los medias puedo lanzar el play
				sendPlay(getMedia().toString());
			}

		}catch(Exception e) {
			createNotify("Exception setupResponse: " + e.getMessage(),false);
		}
	}

	/**
	 * Se ejecutará cuando se reciba un response despues de enviar un PLAY
	 * Comprobará que todo es correcto, y si es así enviará una petición de PLAY
	 * si es que quedan mas stream por los que enviar PLAY, o se notificará
	 * a los observadores de que ya se puede reproducir el media en el caso de que
	 * fuera el último/único stream de PLAY que se esperaba.
	 * @param response
	 */
	public void playResponse(HttpResponse response) {

		logger.trace("RESPUESTA PLAY RECIBIDA");
		try {

			if(!checkCseq(response)) {
				logger.error("error cseq");
				return;
			}

			if(!checkSession(response)){
				logger.error("error session");
				return;
			}

			createNotify("PLAY!",true);	

		}catch(Exception e) {
			createNotify("Exception setupResponse: " + e.getMessage(),false);
		}
	}

	/**
	 * Dado un HttpResponse, comprobará que trae la cabecera CSeq y que 
	 * esta es correcta. 
	 * Devolverá un booleano indicando si es verdadera, en caso contrario
	 * lanzará una excepción que deberá ser controlada desde fuera indicando
	 * el error.
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private boolean checkCseq(HttpResponse response) throws Exception {

		int cSeq_rec;
		String str_cSeq;

		str_cSeq = response.getHeader("CSeq");

		if(str_cSeq==null){
			throw new Exception("Response not contains CSeq header");
		}

		try {
			cSeq_rec = Integer.parseInt(str_cSeq);
		}
		catch(Exception e) {
			throw new Exception("CSeq header must be a number");
		}

		if(cSeq_rec != this.getcSeq()) {
			throw new Exception("CSeq not continue the order");
		}

		setcSeq(cSeq_rec +1 );
		return true;	
	}

	/**
	 * Dado un HttpResponse comprobará que la sesión es la esperada.
	 * En caso de no tener sesión se asignará la que viene en el response, 
	 * en caso contrario comprobará que las sesiones coinciden.
	 * @param response
	 * @return
	 */
	private boolean checkSession(HttpResponse response) {

		String sessionAux = response.getHeader("Session");

		if(session==null) {
			session=sessionAux;
			return true;
		}else if(session.equals(sessionAux)) {
			return true;	
		}else {
			return false;
		}


	}

	/**
	 * Llegada la respuesta a una petición de DESCRIBE, esta debe contener
	 * un SDP. Este método comprobará que el SDP viene y sacará los streams
	 * que éste contiene almacenandolos para poder luego realizar las peticiones
	 * SETUP/PLAY de estos streams.
	 * @param content
	 */
	private void parseSDP(ChannelBuffer content) {

		if (content.readable()) {

			try {

				String str_content = content.toString(CharsetUtil.UTF_8);


				SDPAnnounceParser parser = new SDPAnnounceParser(str_content);
				SessionDescriptionImpl sdp = parser.parse();


				//Meto en el Player el SDP
				setSdp(sdp.toString());

				@SuppressWarnings("unchecked")
				Vector<MediaDescription> vec_md = sdp.getMediaDescriptions(false);

				//Obtengo del SDP los nombres de los medias que contiene.
				Iterator<MediaDescription> it = vec_md.iterator();

				streamsSetup = new ArrayList<String>();
				while(it.hasNext()) {
					MediaDescription md = it.next();
					String stream = md.getAttribute("control");
					streamsSetup.add(stream);
				}

			} catch (ParseException e) {
				createNotify("Parse SDP exception",false);
			} catch(Exception ex) {
				createNotify("Unhandled exception",false);
			}

		}
	}

	/**
	 * Dado un HttpResponse comprobará si trae como contenido un SDP.
	 * En caso contrario lanzará una excepción que deberá se controlada
	 * desde el método que llame a este.
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private boolean checkSDP(HttpResponse response) throws Exception {

		String str_content_type = response.getHeader("Content-Type");

		if ((str_content_type == null) || (!str_content_type.equals("application/sdp"))) {
			throw new Exception("No esta llegando el SDP");
		}
		return true;
	}

	/**
	 * Dada la URL de un media, sacará IP y puerto del servidor,
	 * e inicializará todo lo necesario para comenzar el protocolo
	 * RTSP enviando el primer DESCRIBE.
	 * @param media
	 */
	public void playMedia(String media) {

		try {
			URI uri_media = new URI(media.trim());

			setcSeq(1);
			setSession(null);
			setMedia(uri_media);

			String ip = getIp(media);

			int puerto = getPuerto(media);

			logger.trace("IP:\t"+ip+"\t Puerto:\t"+puerto);

			iniciaCliente(ip,puerto);

			sendDescribe();

		}
		catch(Exception e) {
			createNotify("URI error " + e.getMessage(),false);
		}		
	}

	/**
	 * Dada la URL de un media, devolverá el puerto.
	 * En caso contrario lanzará una excepción que deberá
	 * ser controlada desde fuera.
	 * @param m
	 * @return
	 * @throws Exception 
	 */
	private int getPuerto(String m) throws Exception {
		try {
			m = m.trim();

			if(m.startsWith("rtsp://")) {

				m= m.substring(7);
				String[] tokens1 = m.split("/");

				String aux = tokens1[0];

				String[] tokens2= aux.split(":");

				String str_puerto = tokens2[1];

				int puerto = Integer.parseInt(str_puerto);

				return puerto;
			}
			else {
				throw new Exception("La URI no empieza por rtsp://");
			}

		}catch(Exception e) {
			throw new Exception("La URI no tiene el formato esperado\n" + e.getMessage());
		}

	}

	/**
	 * Dada la URL de un media, devolverá la IP.
	 * En caso contrario lanzará una excepción que deberá
	 * ser controlada desde fuera.
	 * @param m
	 * @return
	 * @throws Exception 
	 */
	private String getIp(String m) throws Exception {

		try {
			m = m.trim();

			if(m.startsWith("rtsp://")) {
				m= m.substring(7);
				String[] tokens1 = m.split("/");

				String aux = tokens1[0];

				String[] tokens2= aux.split(":");

				String ip = tokens2[0];

				return ip;
			}
			else {
				throw new Exception("La URI no empieze por rtsp://");
			}

		}catch(Exception e) {
			throw new Exception("La URI no tiene el formato esperado\n" + e.getMessage());
		}
	}

	/**
	 * Notifica a los observadores que ha ocurrido algo.
	 * Pasa un elemento Info que contendrá la información.
	 * @param string Mensaje de estado
	 * @param b informa si se puede resproducir
	 */
	public void createNotify(String msg, boolean play) {

		if(!play) {
			logger.error(msg);
		}else {
			logger.info(getSdp());
			logger.info(msg);
		}

		Info p = new Info(play, msg, getSdp());

		setChanged();
		notifyObservers(p);
	}

	/**
	 * Este update saltará cuando en la RtspClientStackImpl haya ocurrido un error, la notificación
	 * la levantará RtspRespondeHandler
	 */
	@Override
	public void update(Observable o, Object arg) {

		String msg = "";
		try {
			msg = (String)arg;
		}
		catch(Exception e) {}

		msg = "Server error";

		createNotify(msg,false);
	}
}
