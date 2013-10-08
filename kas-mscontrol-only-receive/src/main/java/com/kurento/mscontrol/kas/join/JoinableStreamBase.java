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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mscontrol.commons.join.JoinableContainer;
import com.kurento.mscontrol.commons.join.JoinableStream;

public abstract class JoinableStreamBase extends JoinableImpl implements
		JoinableStream {

	private JoinableContainer container;
	private StreamType type;

	private class BitrateCalc {
		private class SizeTime {
			private long size; // Bytes
			private long time; // ms

			public SizeTime(long size, long time) {
				this.size = size;
				this.time = time;
			}
		}

		private Queue<SizeTime> q;
		private long computedBytes;
		private long lastComputed;

		public BitrateCalc() {
			this.q = new LinkedList<SizeTime>();
		}

		public long computeBytes(long bytes) {
			long t = System.currentTimeMillis();

			SizeTime st;
			while (!q.isEmpty()) {
				st = q.peek();
				if ((t - st.time) > COMPUTE_TIME) {
					computedBytes -= st.size;
					lastComputed = st.time;
					q.poll();
				} else
					break;
			}

			st = new SizeTime(bytes, t);
			computedBytes += st.size;
			q.offer(st);

			long bitrate = 0;
			if (lastComputed > 0) {
				long tDiff = t - lastComputed;

				if (tDiff > 0)
					bitrate = computedBytes * 8 * 1000 / tDiff;
			}

			return bitrate;
		}
	}

	private long inBitrate;
	private BitrateCalc inBitrateCalc;
	private long outBitrate;
	private BitrateCalc outBitrateCalc;

	private static final int COMPUTE_TIME = 1000; // ms

	protected JoinableStreamBase(JoinableContainer container, StreamType type) {
		this.container = container;
		this.type = type;

		this.inBitrateCalc = new BitrateCalc();
		this.outBitrateCalc = new BitrateCalc();
	}

	@Override
	public JoinableContainer getContainer() {
		return this.container;
	}

	@Override
	public StreamType getType() {
		return this.type;
	}

	private synchronized void setInBitrate(long inBitrate) {
		this.inBitrate = inBitrate;
	}

	public synchronized long getInBitrate() {
		return inBitrate;
	}

	private synchronized void setOutBitrate(long outBitrate) {
		this.outBitrate = outBitrate;
	}

	public synchronized long getOutBitrate() {
		return outBitrate;
	}

	protected void computeInBytes(long inBytes) {
		setInBitrate(inBitrateCalc.computeBytes(inBytes));
	}

	protected void computeOutBytes(long outBytes) {
		setOutBitrate(outBitrateCalc.computeBytes(outBytes));
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
