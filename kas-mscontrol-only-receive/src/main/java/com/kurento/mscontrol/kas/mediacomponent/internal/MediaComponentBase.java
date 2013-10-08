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

import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.kas.join.JoinableContainerImpl;
import com.kurento.mscontrol.kas.mediacomponent.AndroidAction;
import com.kurento.mscontrol.kas.mediacomponent.AndroidInfo;
import com.kurento.mscontrol.kas.mediacomponent.MediaComponentAndroid;

public abstract class MediaComponentBase extends JoinableContainerImpl
		implements MediaComponentAndroid {

	@Override
	public void confirm() throws MsControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public abstract void start() throws MsControlException;

	@Override
	public abstract void stop();

	@Override
	public void onAction(AndroidAction action) throws MsControlException {
		throw new MsControlException("Action not supported");
	}

	@Override
	public Object getInfo(AndroidInfo info) throws MsControlException {
		throw new MsControlException("Info not found");
	}

}
