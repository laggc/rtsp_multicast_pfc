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

package com.kurento.mscontrol.kas.mediacomponent.internal;

import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.kurento.commons.config.Parameters;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.rx.AudioRx;
import com.kurento.kas.media.rx.AudioSamples;
import com.kurento.kas.media.rx.RxPacket;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.join.Joinable;
import com.kurento.mscontrol.kas.join.AudioJoinableStreamImpl_Multicast;

public class AudioRecorderComponent extends RecorderComponentBase implements
		Recorder, AudioRx {


	private static final String LOG_TAG = "AudioRecorderComponent";

	private final int channelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
	private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private AudioTrack audioTrack;
	private final int streamType;

	private RecorderController controller;

	private AudioTrackControl audioTrackControl = null;

	@Override
	public synchronized boolean isStarted() {
		return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
	}

	public AudioRecorderComponent(int maxDelay, boolean syncMediaStreams,
			Parameters params)
			throws MsControlException {
		super(maxDelay, syncMediaStreams);

		if (params == null)
			throw new MsControlException("Parameters are NULL");

		Integer streamType = params.get(STREAM_TYPE).getValue();
		if (streamType == null)
			throw new MsControlException(
					"Params must have AudioRecorderComponent.STREAM_TYPE param.");
		this.streamType = streamType;

		this.packetsQueue = new LinkedBlockingQueue<RxPacket>();
	}

	@Override
	public synchronized void putAudioSamplesRx(AudioSamples audioSamples) {
		long ptsNorm = calcPtsMillis(audioSamples);
		setLastPtsNorm(ptsNorm);
		caclEstimatedStartTime(ptsNorm, audioSamples.getRxTime());
		packetsQueue.offer(audioSamples);
	}

	@Override
	public synchronized void start() throws MsControlException {

		AudioProfile audioProfile = null;

		for (Joinable j : getJoinees(Direction.RECV))
			if (j instanceof AudioJoinableStreamImpl_Multicast) {
				audioProfile = ((AudioJoinableStreamImpl_Multicast) j)
						.getAudioProfile();
			}
		if (audioProfile == null)
			throw new MsControlException("Cannot ger audio profile.");

		int frequency = audioProfile.getSampleRate();

		int minBufferSize = AudioTrack.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);

		audioTrack = new AudioTrack(this.streamType, frequency,
				channelConfiguration, audioEncoding, minBufferSize,
				AudioTrack.MODE_STREAM);

		if (audioTrack != null) {
			audioTrack.play();
		}
		audioTrackControl = new AudioTrackControl();
		audioTrackControl.start();

		setRecording(true);

		controller = getRecorderController();
		controller.addRecorder(this);
	}

	@Override
	public synchronized void stop() {
		stopRecord();
		if (controller != null)
			controller.deleteRecorder(this);

		if (audioTrackControl != null)
			audioTrackControl.interrupt();
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack.release();
			audioTrack = null;
		}
	}

	private class AudioTrackControl extends Thread {
		@Override
		public void run() {
			try {
				AudioSamples audioSamplesProcessed;
				for (;;) {
					if (!isRecording()) {
						synchronized (controll) {
							controll.wait();
						}
						continue;
					}

					if (packetsQueue.isEmpty())
						Log.v(LOG_TAG,
								"Jitter buffer underflow: Audio RX frames queue is empty");

					long targetTime = getTargetTime();
					if (targetTime != -1) {
						long ptsMillis = calcPtsMillis(packetsQueue.peek());
						if ((ptsMillis == -1)
								|| (ptsMillis + getEstimatedStartTime() > (targetTime))) {
							synchronized (controll) {
								controll.wait();
							}
							continue;
						}
					}

					audioSamplesProcessed = (AudioSamples) packetsQueue.take();
					if (audioTrack != null
							&& (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
						audioTrack.write(audioSamplesProcessed.getDataSamples(), 0,
								audioSamplesProcessed.getSize());
					}
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "AudioTrackControl stopped");
			}
		}
	}

}
