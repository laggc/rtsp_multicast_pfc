/*
 * Kurento Android MSControl: MSControl implementation for Android.
 * Copyright (C) 2011  Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.mscontrol.kas.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.sdp.SdpException;

import android.util.Log;

import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.kas.media.ports.MediaPort;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.kas.media.rx.MediaRx;
import com.kurento.kas.media.rx.VideoFrame;
import com.kurento.kas.media.rx.VideoRx;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.join.Joinable;
import com.kurento.mscontrol.kas.mediacomponent.internal.VideoFeeder;
import com.kurento.mscontrol.kas.mediacomponent.internal.VideoRecorder;

public class VideoJoinableStreamImpl_Multicast extends JoinableImpl implements
		VideoRx, VideoFeeder {

	public final static String LOG_TAG = "VideoJoinableStreamMulticast";

	private VideoProfile videoProfile = null;
	private SessionSpec localSessionSpec;
	private MediaPort videoMediaPort;

	private VideoRxThread videoRxThread = null;


	private Set<int[]> freeFrames;
	private Map<int[], Integer> usedFrames;

	private static final double MEMORY_TO_USE = 0.6;
	private long maxMemory;
	private long memoryUsed;

	public VideoProfile getVideoProfile() {
		return videoProfile;
	}

	public VideoJoinableStreamImpl_Multicast(
			SessionSpec localSessionSpec, MediaPort videoMediaPort,
			Integer maxDelayRx) {

		super();
		this.localSessionSpec = localSessionSpec;
		this.videoMediaPort = videoMediaPort;


		this.videoRxThread = new VideoRxThread(this, maxDelayRx);
		this.videoRxThread.start();

		this.freeFrames = new CopyOnWriteArraySet<int[]>();
		this.usedFrames = new HashMap<int[], Integer>();

		maxMemory = (long) (Runtime.getRuntime().maxMemory() * MEMORY_TO_USE);
	}



	@Override
	public synchronized void putVideoFrameRx(VideoFrame videoFrame) {
	
		int n = 1;
		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof VideoRecorder) {
					usedFrames.put(videoFrame.getDataFrame(), n++);
					((VideoRecorder) j).putVideoFrame(videoFrame, this);
				}
		} catch (MsControlException e) {

			e.printStackTrace();
		}
	}

	@Override
	public synchronized void freeVideoFrameRx(VideoFrame videoFrame) {

		Log.d(LOG_TAG, "freeVideoFrameRx");
		if (videoFrame == null || usedFrames == null)
			return;

		Integer count = usedFrames.get(videoFrame.getDataFrame());
		if (count == null)
			return;
		if (--count == 0) {
			usedFrames.remove(videoFrame.getDataFrame());
			freeFrames.add(videoFrame.getDataFrame());
		} else
			usedFrames.put(videoFrame.getDataFrame(), count);
	}

	// TODO: improve memory (video frame buffers) management.
	private int[] createFrameBuffer(int length) {
		int[] buffer = null;

		long size = length * Integer.SIZE / 8;

		try {
			if (memoryUsed < maxMemory) {
				buffer = new int[length];
				memoryUsed += size;
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			Log.w(LOG_TAG, e);
			buffer = null;
		}

		if (buffer == null)
			Log.w(LOG_TAG, "Can not create frame buffer. No such memory.");

		return buffer;
	}

	@Override
	public synchronized int[] getFrameBuffer(int size) {
		if (size % (Integer.SIZE / 8) != 0) {
			Log.w(LOG_TAG, "Size must be multiple of " + (Integer.SIZE / 8));
			return null;
		}

		int l = size / (Integer.SIZE / 8);
		if (freeFrames.isEmpty())
			return createFrameBuffer(l);

		for (int[] b : freeFrames) {
			freeFrames.remove(b);
			if (b.length >= l)
				return b;
		}

		return createFrameBuffer(l);
	}

	public void stop() {
		Log.d(LOG_TAG, "stopVideoRx");
		MediaRx.stopVideoRx();

		freeFrames.clear();
		usedFrames.clear();

		System.gc();

		Log.i(LOG_TAG, "freeMemory: " + Runtime.getRuntime().freeMemory()/1024
				+ "KB maxMemory: " + Runtime.getRuntime().maxMemory()/1024
				+ "KB totalMemory: " + Runtime.getRuntime().totalMemory()/1024 + "KB");
	}

	private class VideoRxThread extends Thread {
		private VideoRx videoRx;
		private int maxDelayRx;

		public VideoRxThread(VideoRx videoRx, int maxDelayRx) {
			this.videoRx = videoRx;
			this.maxDelayRx = maxDelayRx;
		}

		@Override
		public void run() {
			Log.d(LOG_TAG, "startVideoRx");
			SessionSpec s = filterMediaByType(localSessionSpec, MediaType.VIDEO);

			if (!s.getMedias().isEmpty()) {

				try {

					String sdpVideo = SdpConversor.sessionSpec2Sdp(s);

					Integer aux = MediaRx.startVideoRx(videoMediaPort,
							sdpVideo,
							maxDelayRx,
							this.videoRx);
					Log.d(LOG_TAG,
							"Error MediaRx.startVideoRx: " + aux.toString());
				} catch (SdpException e) {
					Log.e(LOG_TAG, "Could not start video rx " + e.toString());
				} catch (Exception e) {
					Log.e(LOG_TAG, "Error: " + e.toString());
				}
			}
		}
	}

	protected static SessionSpec filterMediaByType(SessionSpec session,
			MediaType type) {
		List<MediaSpec> mediaList = new ArrayList<MediaSpec>();

		for (MediaSpec m : session.getMedias()) {
			Set<MediaType> mediaTypes = m.getType();
			if (mediaTypes.size() != 1)
				continue;
			for (MediaType t : mediaTypes) {
				if (t == type) {
					for (Payload p : m.getPayloads()) {
						MediaSpec newM = new MediaSpec(null, m.getType(),
								m.getTransport(), m.getDirection());
						newM.addToPayloads(p);
						mediaList.add(newM);
						break;
					}
				}
				break;
			}
		}

		return new SessionSpec(mediaList, "-");
	}

}
