package org.mobicents.rtsp;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author amit.bhayani
 * 
 */
@Sharable
public class RtspResponseHandler extends SimpleChannelUpstreamHandler {

	private static Logger logger = LoggerFactory.getLogger(RtspResponseHandler.class);

	private final RtspClientStackImpl rtspClientStackImpl;


	public RtspResponseHandler(RtspClientStackImpl rtspClientStackImpl) {
		this.rtspClientStackImpl = rtspClientStackImpl;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {

		logger.debug("Channel connected.");
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		logger.debug("Message received.");

		HttpResponse httpResponse = (HttpResponse) e.getMessage();
		rtspClientStackImpl.processRtspResponse(httpResponse);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		
		logger.error(e.getCause().getMessage());
		
		//Notifico el error
		rtspClientStackImpl.exceptionCaught(e.getCause().getMessage());
		
		e.getChannel().close();
		//super.exceptionCaught(ctx, e);
	}
}