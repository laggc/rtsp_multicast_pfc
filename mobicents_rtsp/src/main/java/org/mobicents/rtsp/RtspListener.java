package org.mobicents.rtsp;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * 
 * @author amit.bhayani
 * 
 */
public interface RtspListener {
	public void onRtspRequest(HttpRequest request, Channel chanel);

	public void onRtspResponse(HttpResponse response);
}
