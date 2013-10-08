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

import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.join.JoinableContainer;
import com.kurento.mscontrol.commons.join.JoinableStream;
import com.kurento.mscontrol.commons.join.JoinableStream.StreamType;

public class JoinableContainerImpl extends JoinableImpl implements
		JoinableContainer {

	protected JoinableStream[] streams;

	@Override
	public JoinableStream getJoinableStream(StreamType value)
			throws MsControlException {
		if (streams == null)
			return null;

		for (JoinableStream s : streams) {
			if (s != null && s.getType().equals(value)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public JoinableStream[] getJoinableStreams() throws MsControlException {
		return streams;
	}

}
