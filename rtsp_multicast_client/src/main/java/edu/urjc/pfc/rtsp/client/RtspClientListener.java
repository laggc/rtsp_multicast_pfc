package edu.urjc.pfc.rtsp.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.mobicents.rtsp.RtspListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author laggc
 *
 */
public class RtspClientListener implements RtspListener {

	private static Logger logger =LoggerFactory.getLogger(RtspClientListener.class);

	private ClientRTSP client;

	public RtspClientListener(ClientRTSP client) {

		this.client = client;
	}

	public void onRtspRequest(HttpRequest request, Channel chanel) {
		logger.trace("Received request " + request);
	}

	/**
	 * Miramos el comando que hemos enviado para tratar la respuesta recibida de
	 * forma correcta
	 */
	public void onRtspResponse(HttpResponse response) {

		HttpResponseStatus status = response.getStatus();

		if (!status.equals(HttpResponseStatus.OK)) {

			logger.error("Response Error:\t" + status.getReasonPhrase());
			client.createNotify("Response Error:\t" + status.getReasonPhrase(),false);
			client.setCmd_sent(null);
			return;
		}

		if (client.getCmd_sent().equals(RtspMethods.DESCRIBE)) {
			client.describeResponse(response);
			return;
		}

		if (client.getCmd_sent().equals(RtspMethods.SETUP)) {
			client.setupResponse(response);
			return;
		}

		if (client.getCmd_sent().equals(RtspMethods.PLAY)) {
			client.playResponse(response);
			return;

		}
	}
}
