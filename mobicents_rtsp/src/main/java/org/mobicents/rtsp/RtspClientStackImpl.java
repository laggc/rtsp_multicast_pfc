package org.mobicents.rtsp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author amit.bhayani
 * 
 */
public class RtspClientStackImpl extends Observable implements RtspStack {

	private static Logger logger = LoggerFactory.getLogger(RtspClientStackImpl.class);

	private final String address;
	private final int port;
	private final InetAddress inetAddress;
	private Channel channel = null;
	private ClientBootstrap bootstrap;

	private RtspListener listener = null;

	public RtspClientStackImpl(String address, int port) throws UnknownHostException {
		this.address = address;
		this.port = port;
		this.inetAddress = InetAddress.getByName(this.address);
	}

	public String getAddress() {
		return this.address;
	}

	public int getPort() {
		return this.port;
	}

	public void start() {

		InetSocketAddress bindAddress = new InetSocketAddress(this.inetAddress, this.port);

		// Configure the client.
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new RtspClientPipelineFactory(this));

		logger.info("Iniciado el cliente RTSP con el servidor:\t"+ bindAddress.toString());

	}

	protected void processRtspResponse(HttpResponse httpResponse) {
		synchronized (this.listener) {
			listener.onRtspResponse(httpResponse);
		}
	}

	protected void processRtspRequest(RtspRequest rtspRequest, Channel channel) {
		synchronized (this.listener) {
			listener.onRtspRequest(rtspRequest, channel);
		}
	}

	public void stop() {
		ChannelFuture cf = channel.getCloseFuture();
		cf.addListener(new ClientChannelFutureListener());

		channel.close();
		cf.awaitUninterruptibly();
		try {
			bootstrap.getFactory().releaseExternalResources();
		}catch(Exception e){}

	}

	public void setRtspListener(RtspListener listener) {
		this.listener = listener;

	}

	/**
	 * Esta funcion ha sido muy modificada.
	 * En caso de error revisar la original
	 * @throws Exception 
	 */
	public void sendRquest(RtspRequest rtspRequest) throws Exception {

		ChannelFuture future = null;
		if (channel == null || (channel != null && !channel.isConnected())) {

			logger.debug("Client connection");

			String host = rtspRequest.getHost();
			int port = rtspRequest.getPort();
			InetSocketAddress isa =new InetSocketAddress(host,port);
			future = bootstrap.connect(isa);


			final CountDownLatch channelLatch = new CountDownLatch(1);
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture cf) throws Exception {
					if(cf.isSuccess()) {
						channel = cf.getChannel();
						channelLatch.countDown();
					} else {
						bootstrap.releaseExternalResources();
						logger.error("Conexion rechazada por el servidor.");
						throw new Exception("Conexion rechazada por el servidor.");
					}
				}
			});

			try {
				channelLatch.await();
			} catch(InterruptedException ex) {
				logger.error("Conexion rechazada por el servidor.");
				throw new Exception("Interrupted while waiting for streaming connection to arrive.");
			}
		}
		
		logger.debug("Client write request");
		channel.write(rtspRequest);
	}

	private class ClientChannelFutureListener implements ChannelFutureListener {

		public void operationComplete(ChannelFuture arg0) throws Exception {
			logger.info("Mobicents RTSP Client Stop complete");
		}
	}

	/**
	 * Cuando RtspResponseHandler capture un error, invocará a esta función la cual
	 * notificará a los observadores, ClientRTSP, que ha ocurrido un error.
	 * @param message
	 */
	public void exceptionCaught(String message) {
		setChanged();
		notifyObservers(message);
		
	}
}