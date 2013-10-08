package edu.urjc.pfc.rtsp.server;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Clase excepcion creada para cuando hay un error parseando un mensaje
 * de alguno de los clientes. 
 * Contiene el HttpResponseStatus que se enviará como motivo del error.
 * @author laggc
 *
 */
public class RtspRequestException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private HttpResponseStatus status;
	
	public HttpResponseStatus getStatus() {
		return status;
	}

	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}

	
	public RtspRequestException() {
		super();
	}
	
	public RtspRequestException(String msg, HttpResponseStatus status) {
		super(msg);
		setStatus(status);
	}

}
