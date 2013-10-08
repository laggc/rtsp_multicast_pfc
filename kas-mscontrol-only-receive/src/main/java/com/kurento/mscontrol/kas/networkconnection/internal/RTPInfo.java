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

package com.kurento.mscontrol.kas.networkconnection.internal;

import java.util.List;
import java.util.Set;

import android.util.Log;

import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.exception.CodecNotSupportedException;
import com.kurento.mediaspec.Direction;
import com.kurento.mediaspec.Fraction;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mediaspec.Transport;
import com.kurento.mediaspec.TransportRtp;

public class RTPInfo {

	public final static String LOG_TAG = "RTPInfo";

	private String dstIp;

	private Direction videoMode;
	private int dstVideoPort;
	private VideoCodecType videoCodecType;
	private int videoPayloadType = -1;
	private int videoBandwidth = -1;
	private int frameWidth = -1;
	private int frameHeight = -1;
	private Fraction frameRate = null;

	private Direction audioMode;
	private int dstAudioPort;
	private AudioCodecType audioCodecType;
	private int audioPayloadType;

	public String getDstIp() {
		return dstIp;
	}

	public Direction getVideoMode() {
		return videoMode;
	}

	public int getDstVideoPort() {
		return dstVideoPort;
	}

	public VideoCodecType getVideoCodecType() {
		return videoCodecType;
	}

	public int getVideoPayloadType() {
		return videoPayloadType;
	}

	public int getVideoBandwidth() {
		return videoBandwidth;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public Fraction getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(Fraction frameRate) {
		this.frameRate = frameRate;
	}

	public Direction getAudioMode() {
		return audioMode;
	}

	public int getDstAudioPort() {
		return dstAudioPort;
	}

	public AudioCodecType getAudioCodecType() {
		return audioCodecType;
	}

	public int getAudioPayloadType() {
		return audioPayloadType;
	}

	public RTPInfo(SessionSpec se) {
		Log.d(LOG_TAG, "sessionSpec:\n" + se);

		List<MediaSpec> medias = se.getMedias();
		if (medias.isEmpty())
			return;

		for (MediaSpec m : medias) {
			if (!m.isSetTransport()) {
				Log.w(LOG_TAG, "Media does not have transport");
				continue;
			}

			Transport tr = m.getTransport();
			if (!tr.isSetRtp()) {
				Log.w(LOG_TAG, "Transport does not have transportRtp");
				continue;
			}

			TransportRtp trtp = tr.getRtp();
			if (!trtp.isSetAddress()) {
				Log.w(LOG_TAG, "TransportRtp does not have address");
				continue;
			}

			this.dstIp = trtp.getAddress();
			break;
		}

		videoMode = Direction.INACTIVE;
		audioMode = Direction.INACTIVE;

		for (MediaSpec m : medias) {
			Set<MediaType> mediaTypes = m.getType();
			if (mediaTypes.size() != 1)
				continue;
			for (MediaType t : mediaTypes) {
				if (Direction.INACTIVE.equals(audioMode)
						&& t == MediaType.AUDIO) {
					audioMode = m.getDirection();
					if (Direction.INACTIVE.equals(audioMode))
						continue;

					if (!m.isSetTransport() || !m.getTransport().isSetRtp()
							|| !m.getTransport().getRtp().isSetPort()) {
						Log.w(LOG_TAG, "Can not get port for audio");
						continue;
					}

					this.dstAudioPort = m.getTransport().getRtp().getPort();

					List<Payload> payloads = m.getPayloads();
					if ((payloads != null) && !payloads.isEmpty()) {
						Payload p = payloads.get(0);
						String encodingName = "";
						try {
							if (p.isSetRtp()) {
								PayloadRtp rtpInfo = p.getRtp();
								if (rtpInfo.isSetCodecName()) {
									encodingName = rtpInfo.getCodecName();
									this.audioCodecType = AudioCodecType
											.getCodecTypeFromName(encodingName);
								}
								if (rtpInfo.isSetId())
									this.audioPayloadType = rtpInfo.getId();
							}
						} catch (CodecNotSupportedException e) {
							Log.w(LOG_TAG, encodingName + " not supported.");
						}
					}
				} else if (Direction.INACTIVE.equals(videoMode)
						&& t == MediaType.VIDEO) {
					videoMode = m.getDirection();
					if (Direction.INACTIVE.equals(videoMode))
						continue;
					if (!m.isSetTransport() || !m.getTransport().isSetRtp()
							|| !m.getTransport().getRtp().isSetPort()) {
						Log.w(LOG_TAG, "Can not get port for video");
						continue;
					}

					this.dstVideoPort = m.getTransport().getRtp().getPort();

					List<Payload> payloads = m.getPayloads();
					if ((payloads != null) && !payloads.isEmpty()) {
						Payload p = payloads.get(0);
						String encodingName = "";
						try {
							if (p.isSetRtp()) {
								PayloadRtp payRtp = p.getRtp();
								if (payRtp.isSetCodecName()) {
									encodingName = payRtp.getCodecName();
									this.videoCodecType = VideoCodecType
											.getCodecTypeFromName(encodingName);
								}
								if (payRtp.isSetId())
									this.videoPayloadType = payRtp.getId();
								if (payRtp.isSetBitrate())
									this.videoBandwidth = payRtp.getBitrate();
								if (payRtp.isSetWidth())
									this.frameWidth = payRtp.getWidth();
								if (payRtp.isSetHeight())
									this.frameHeight = payRtp.getHeight();
								if (payRtp.isSetFramerate())
									this.frameRate = payRtp.getFramerate();
							}
						} catch (CodecNotSupportedException e) {
							Log.w(LOG_TAG, encodingName + " not supported.");
						}
					}
				}
				break;
			}
		}
	}

	public String getVideoRTPDir() {
		return "rtp://" + dstIp + ":" + dstVideoPort;
	}

	public String getAudioRTPDir() {
		return "rtp://" + dstIp + ":" + dstAudioPort;
	}

}
