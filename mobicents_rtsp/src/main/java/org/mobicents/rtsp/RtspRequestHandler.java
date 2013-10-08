package org.mobicents.rtsp;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author amit bhayani
 * 
 */
@SuppressWarnings("deprecation")
@ChannelPipelineCoverage("one")
public class RtspRequestHandler extends SimpleChannelUpstreamHandler {

	private static Logger logger = LoggerFactory.getLogger(RtspRequestHandler.class);

	private final RtspServerStackImpl rtspServerStackImpl;

	protected RtspRequestHandler(RtspServerStackImpl rtspServerStackImpl) {
		this.rtspServerStackImpl = rtspServerStackImpl;
	}

	private volatile RtspRequest request;
	private volatile boolean readingChunks;
	private final StringBuilder responseContent = new StringBuilder();


	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.debug("Channel connected.");
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		logger.debug("Message received.");

		HttpRequest httpRequest = (HttpRequest) e.getMessage();
		rtspServerStackImpl.processRtspRequest(httpRequest, e.getChannel());

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.error("ExceptionCaught");
		logger.error(e.getCause().getMessage());
		e.getChannel().close();
	}

	public void setRequest(RtspRequest request) {
		this.request = request;
	}

	public RtspRequest getRequest() {
		return request;
	}

	public void setReadingChunks(boolean readingChunks) {
		this.readingChunks = readingChunks;
	}

	public boolean isReadingChunks() {
		return readingChunks;
	}

	public StringBuilder getResponseContent() {
		return responseContent;
	}


}
