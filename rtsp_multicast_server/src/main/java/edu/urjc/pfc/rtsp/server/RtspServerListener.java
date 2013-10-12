package edu.urjc.pfc.rtsp.server;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.jboss.netty.handler.codec.rtsp.RtspResponseStatuses;
import org.jboss.netty.handler.codec.rtsp.RtspVersions;
import org.mobicents.rtsp.DefaultRtspResponse;
import org.mobicents.rtsp.RtspListener;
import org.mobicents.rtsp.RtspResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtspServerListener implements RtspListener {

	private static Logger logger = LoggerFactory.getLogger(RtspServerListener.class);

	private ServerRTSP server;

	/**
	 * Constructor de la clase.
	 * Entra como parámetro la instancia del servidor para
	 * poder usar alguno de sus métodos
	 * @param server_
	 */
	public RtspServerListener(ServerRTSP server_) {
		server=server_;
	}

	/**
	 * Metodo que se ejecutará al recibir un request de un cliente.
	 */
	public void onRtspRequest(HttpRequest request, Channel channel) {

		int cSeq=1;
		RtspResponse response = null;
		HttpMethod method = null;

		try {
			cSeq = getcSeq(request);
			
			checkProtocol(request);
			method= getMethod(request);


			if(method.equals(RtspMethods.DESCRIBE)) {
				logger.info("Recibido request: DESCRIBE");
				describeRequest(request,channel,cSeq);
				return;
			}

			if(method.equals(RtspMethods.SETUP)) {
				logger.info("Recibido request: SETUP");
				setupRequest(request,channel,cSeq);
				return;
			}

			if(method.equals(RtspMethods.PLAY)) {
				logger.info("Recibido request: PLAY");
				playRequest(request,channel,cSeq);
				return;
			}

			if(method.equals(RtspMethods.OPTIONS)) {
				logger.info("Recibido request: OPTIONS");
				optionsRequest(request,channel,cSeq);
				return;
			}

			logger.error("Recibido request con comando desconocido.");
			throw new RtspRequestException("Method not allowed", RtspResponseStatuses.NOT_IMPLEMENTED);
			

		}
		catch(RtspRequestException e) {

			response = new DefaultRtspResponse(e.getStatus());
			response.addHeader("CSeq", cSeq);
			sendResponse(response,channel);

			return;
		}
	}




	/**
	 * Se ejecutará cuando recibamos una peticion de DESCRIBE.
	 * Si hay algun error lanzará una RtspRequestException que será capturada
	 * por el método onRtspRequest que lanzará un mensaje de error al cliente.
	 * @param request
	 * @param channel
	 * @param cSeq
	 * @throws RtspRequestException
	 */
	private void describeRequest(HttpRequest request, Channel channel, int cSeq) throws RtspRequestException  {

		//Extraemos la URI
		String uri = request.getUri();

		//Extraemos el media de la URI
		Media media = getMedia(uri);

		logger.info("Describe request a: " + uri);

		if(media==null){
			throw new RtspRequestException("Media not found",RtspResponseStatuses.SERVICE_UNAVAILABLE);	
		}

		//Extraemos el SDP
		String sdp = media.getSdp();

		//Creamos la respuesta afirmativa.
		RtspResponse response = new DefaultRtspResponse(RtspResponseStatuses.OK);
		response.addHeader("CSeq",cSeq);
		response.addHeader("Content-Type", "application/sdp");
		response.addHeader("Content-Length", sdp.length());
		ChannelBuffer sdpBuffer = ChannelBuffers.copiedBuffer(sdp, Charset.defaultCharset());
		response.setContent(sdpBuffer);

		//Enviamos la respuesta afirmativa.
		sendResponse(response, channel);
	}

	/**
	 * Se ejecutará cuando recibamos una peticion de SETUP
	 * Si hay algun error lanzará una RtspRequestException que será capturada
	 * por el método onRtspRequest que lanzará un mensaje de error al cliente.
	 * @param request
	 * @param channel
	 * @param cSeq
	 * @throws RtspRequestException
	 */
	private void setupRequest(HttpRequest request, Channel channel, int cSeq) throws RtspRequestException {

		//Extreemos la URI
		String uri = request.getUri();

		//Comprobamos que la cabecera transport que se envia en la peticion es de tipo MULTICAST
		checkTransport(request);

		//Generamos el contenido de la cabecera de transporte que enviaremos
		String transport = generateTransport(uri);

		//Extraemos la sesion en caso de que ya haya un ID de sesión, y si no  genera una nueva
		String session= getSession(request, RtspMethods.SETUP );

		logger.info("Setup request a: " + uri);

		//Comprobamos que el ID de sesión esta en la lista del servidor.
		if(!checkSession(session)) {
			throw new RtspRequestException("Session not found",RtspResponseStatuses.SESSION_NOT_FOUND);
		}

		//En caso de que el transport generado sea null, es que no se ha encontrado el media.
		if(transport==null){
			throw new RtspRequestException("Stream not found",RtspResponseStatuses.SERVICE_UNAVAILABLE);	
		}

		//Generamos la respuesta positiva al SETUP
		RtspResponse response = new DefaultRtspResponse(RtspResponseStatuses.OK);
		response.addHeader("CSeq",cSeq);
		response.addHeader("Transport", transport);
		response.addHeader("Session", session);

		//Enviamos la respuesta
		sendResponse(response, channel);
	}

	/**
	 * Se ejecutará cuando recibamos una petición de PLAY
	 * Si hay algun error lanzará una RtspRequestException que será capturada
	 * por el método onRtspRequest que lanzará un mensaje de error al cliente.
	 * @param request
	 * @param channel
	 * @param cSeq
	 * @throws RtspRequestException
	 */
	private void playRequest(HttpRequest request, Channel channel, int cSeq) throws RtspRequestException {

		//Extraemos la URI
		String uri = request.getUri();

		//Extraemos el ID de sesión
		String session= getSession(request, RtspMethods.PLAY);

		//Comprobamos que existe el stream que se pdie
		if(!checkStream(uri)) {
			throw new RtspRequestException("Stream not found.",RtspResponseStatuses.SERVICE_UNAVAILABLE);
		}

		//Comprobamos que el ID de sesión existe
		if(!checkSession(session)) {
			throw new RtspRequestException("Session not found.",RtspResponseStatuses.SESSION_NOT_FOUND);
		}

		//Generamos la respuesta positiva al SETUP
		RtspResponse response = new DefaultRtspResponse(RtspResponseStatuses.OK);
		response.addHeader("CSeq",cSeq);
		response.addHeader("Session", session);

		//Enviamos la respuesta
		sendResponse(response, channel);	
	}

	private void optionsRequest(HttpRequest request, Channel channel, int cSeq) throws RtspRequestException {

		RtspResponse response = new DefaultRtspResponse(RtspResponseStatuses.OK);
		response.addHeader("CSeq",cSeq);
		response.addHeader("Public", "DESCRIBE, SETUP, PLAY");

		//Enviamos la respuesta
		sendResponse(response, channel);

	}

	/**
	 * Metodo que se encarga de enviar la respuesta.
	 * @param response
	 * @param channel
	 */
	private void sendResponse(RtspResponse response, Channel channel) {
		try {

			String date = new Timestamp(new Date().getTime()).toString();
			String host_remote = channel.getRemoteAddress().toString();

			logger.info(date + "\t" + host_remote + "\t"+response.getStatus().toString());

			channel.write(response);
		}
		catch(Exception e) {
			logger.error("[SERVER]\tProblem sending.");
		}
	}


	/**
	 * Metodo que extrae la cabecera de una peticion el CSeq
	 */
	private int getcSeq(HttpRequest request) throws RtspRequestException {

		int cSeq;
		String str_cSeq;


		str_cSeq = request.getHeader("CSeq");

		if(str_cSeq==null){
			throw new RtspRequestException("Request not contains CSeq header", RtspResponseStatuses.BAD_REQUEST);
		}

		try {
			cSeq = Integer.parseInt(str_cSeq);
		}
		catch(Exception e) {
			throw new RtspRequestException("CSeq header must be a number", RtspResponseStatuses.BAD_REQUEST);
		}

		return cSeq;	
	}

	/**
	 * Comprobamos en las peticiones que el protocolo RTSP es aceptado.
	 * @param request
	 * @throws RtspRequestException
	 */
	private void checkProtocol(HttpRequest request) throws RtspRequestException {

		if(request.getProtocolVersion() != RtspVersions.RTSP_1_0) {
			throw new RtspRequestException("RTSP Version not supported", RtspResponseStatuses.RTSP_VERSION_NOT_SUPPORTED);
		}
	}

	/**
	 * De una petición extraemos el método.
	 * @param request
	 * @return
	 * @throws RtspRequestException
	 */
	private HttpMethod getMethod(HttpRequest request) throws RtspRequestException {

		HttpMethod method = request.getMethod();

		if(method == null) {
			throw new RtspRequestException("Method not found", RtspResponseStatuses.METHOD_NOT_VALID);
		}

		if( !((method==RtspMethods.DESCRIBE) || (method==RtspMethods.SETUP) || (method==RtspMethods.PLAY) || (method==RtspMethods.OPTIONS))  ) {
			throw new RtspRequestException("Method not allowed", RtspResponseStatuses.METHOD_NOT_ALLOWED);
		}

		return method;	
	}

	/**
	 * Verificamos que las peticiones SETUP expecifican en la cabecera
	 * Transport multicast.
	 * @param request
	 * @throws RtspRequestException
	 */
	private void checkTransport(HttpRequest request) throws RtspRequestException {

		String transport = request.getHeader("Transport");

		if(transport==null) {
			throw new RtspRequestException("Request not contains Transport header", RtspResponseStatuses.BAD_REQUEST);
		}

		
		
		if(!transport.toUpperCase().contains("MULTICAST")) {
			throw new RtspRequestException("Only multicast transport is allowed", RtspResponseStatuses.UNSUPPORTED_TRANSPORT);
		}
		
		if(transport.toUpperCase().contains("TCP")) {
			throw new RtspRequestException("Only UDP transport is allowed", RtspResponseStatuses.UNSUPPORTED_TRANSPORT);
		}
	}

	/**
	 * Dada una URI, buscamos en el servidor el media y el stream
	 * pedidos, y el objeto Media generará la cadena Transport.
	 * @param uri
	 * @return
	 */
	private String generateTransport(String uri) {

		//Limpiamos la URI de la IP y el puerto
		try {
			uri = uri.trim();

			if(uri.startsWith("rtsp://")) {
				uri= uri.substring(7);
				String[] tokens1 = uri.split("/");

				int n = tokens1.length;
				String stream = tokens1[n-1];
				String title = tokens1[n-2];

				Media m = server.getMedia(title);

				return m.generateTransport(stream);

			}
			else {
				throw new Exception("Error obteniendo el transport");
			}

		}catch(Exception e) {
			logger.error("La URI no tiene el formato esperado\n" + e.getMessage());
			System.exit(-1);
		}

		return null;
	}


	/**
	 * Comprobamos si existe un Stream a partir de una URI.
	 * Hacemos uso del medio generateTransport
	 * @param uri
	 * @return
	 */
	private boolean checkStream(String uri) {
		//Limpiamos la URI de la IP y el puerto
		try {
			uri = uri.trim();

			if(uri.startsWith("rtsp://")) {
				uri= uri.substring(7);
				String[] tokens1 = uri.split("/");

				int n = tokens1.length;
		
				String title = n==2 ? tokens1[n-1] : null;
				
				if(title == null) { 
					return false;
				}
				
				Media m = server.getMedia(title);
				
				return (m==null?false:true);
			
			}
			else {
				throw new Exception("La URI no empieza por rtsp://");
			}

		}catch(Exception e) {
			logger.error("La URI no tiene el formato esperado\n" + e.getMessage());
			System.exit(-1);
		}

		return false;
	}

	/**
	 * Genera aleatoriamente un identificador de sesión
	 * @param channel
	 * @return
	 */
	private synchronized String generateSession() {

		int length = 10;
		long seed = new java.util.GregorianCalendar().getTimeInMillis();
		Random r = new Random(seed);
		StringBuffer sb_session = new StringBuffer();
		String session;

		int i = 0;
		while (i < length){
			long aux = r.nextInt(Integer.MAX_VALUE)%9;
			sb_session.append(aux);
			i++;
		}
		session=sb_session.toString();

		if(!checkSession(session)){
			server.addSession(session);
			return session;
		}
		else
			return generateSession();
	}

	/**
	 * De una petición extraemos el ID de sesión si tiene, o si no, 
	 * generamos uno.
	 * @param request
	 * @param method
	 * @return
	 * @throws RtspRequestException
	 */
	private String getSession(HttpRequest request, HttpMethod method) throws RtspRequestException {

		String session = request.getHeader("Session");

		if((session==null) && method.equals(RtspMethods.PLAY)) {
			throw new RtspRequestException("Session not found in play request",RtspResponseStatuses.SESSION_NOT_FOUND);
		}

		if(session==null)
		{
			session=generateSession();
		}
		return session;
	}

	/**
	 * Comprobamos si un ID de sesión está en la lista de IDs del servidor.
	 * @param session
	 * @return
	 */
	private synchronized boolean checkSession(String session) {

		return server.checkSession(session);
	}

	/**
	 * Dada una URI, extraemos el nombre del Media.
	 * @param uri
	 * @return
	 */
	private Media getMedia(String uri) {

		//Limpiamos la URI de la IP y el puerto
		try {
			uri = uri.trim();

			if(uri.startsWith("rtsp://")) {
				uri= uri.substring(7);
				String[] tokens1 = uri.split("/");

				int n = tokens1.length - 1;
				String title = tokens1[n];

				return server.getMedia(title);	
			}
			else {
				throw new Exception();
			}

		}catch(Exception e) {
			logger.error("La URI no tiene el formato esperado\n" + e.getMessage());
			System.exit(-1);
		}

		return null;
	}

	public void onRtspResponse(HttpResponse response) {

	}

}