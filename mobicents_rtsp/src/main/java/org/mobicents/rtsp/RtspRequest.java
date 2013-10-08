package org.mobicents.rtsp;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * 
 * @author amit bhayani
 *
 */
public interface RtspRequest extends HttpRequest {
	public String getHost();
	
	public int getPort();

	public String debug();
	
	public HttpMethod getMethod();
	

}
