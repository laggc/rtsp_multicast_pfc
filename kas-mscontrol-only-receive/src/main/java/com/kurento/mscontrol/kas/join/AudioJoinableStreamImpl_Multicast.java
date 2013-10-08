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
import java.util.List;
import java.util.Set;

import javax.sdp.SdpException;

import android.util.Log;

import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.ports.MediaPort;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.rx.AudioRx;
import com.kurento.kas.media.rx.AudioSamples;
import com.kurento.kas.media.rx.MediaRx;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.join.Joinable;
import com.kurento.mscontrol.kas.networkconnection.internal.RTPInfo;

public class AudioJoinableStreamImpl_Multicast extends JoinableImpl implements
		AudioRx {

	public final static String LOG_TAG = "AudioJoinableStream_Multicast";

	private AudioProfile audioProfile;
	private SessionSpec localSessionSpec;
	private MediaPort audioMediaPort;

	private AudioRxThread audioRxThread = null;


	public AudioProfile getAudioProfile() {
		return audioProfile;
	}

	public AudioJoinableStreamImpl_Multicast(SessionSpec localSessionSpec,
			MediaPort audioMediaPort,
			Integer maxDelayRx) {
		super();
		this.localSessionSpec = localSessionSpec;
		this.audioMediaPort = audioMediaPort;


		
		RTPInfo remoteRTPInfo = new RTPInfo(localSessionSpec);
		AudioCodecType audioCodecType = remoteRTPInfo.getAudioCodecType();
		audioProfile = AudioProfile
				.getAudioProfileFromAudioCodecType(audioCodecType);
		
		
		this.audioRxThread = new AudioRxThread(this, maxDelayRx);
		this.audioRxThread.start();
	}

	@Override
	public void putAudioSamplesRx(AudioSamples audioSamples) {

		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof AudioRx) {
					((AudioRx) j).putAudioSamplesRx(audioSamples);
				}
		} catch (MsControlException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		Log.d(LOG_TAG, "stopAudioRx");
		MediaRx.stopAudioRx();
	}

	private class AudioRxThread extends Thread {
		private AudioRx audioRx;
		private int maxDelayRx;

		public AudioRxThread(AudioRx audioRx, int maxDelayRx) {
			this.audioRx = audioRx;
			this.maxDelayRx = maxDelayRx;
		}

		@Override
		public void run() {
			Log.d(LOG_TAG, "startAudioRx");
			SessionSpec s = filterMediaByType(localSessionSpec, MediaType.AUDIO);
			if (!s.getMedias().isEmpty()) {
				try {
					String sdpAudio = SdpConversor.sessionSpec2Sdp(s);
					MediaRx.startAudioRx(audioMediaPort, sdpAudio, maxDelayRx,
							this.audioRx);

				} catch (SdpException e) {
					Log.e(LOG_TAG, "Could not start audio rx " + e.toString());
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
