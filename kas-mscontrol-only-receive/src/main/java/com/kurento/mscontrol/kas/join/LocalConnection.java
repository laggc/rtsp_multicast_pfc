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

import com.kurento.mscontrol.commons.join.Joinable;
import com.kurento.mscontrol.commons.join.Joinable.Direction;

public class LocalConnection {

	private Direction direction;
	private Joinable joinable;
	private LocalConnection other;

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Joinable getJoinable() {
		return joinable;
	}

	public void setJoinable(Joinable joinable) {
		this.joinable = joinable;
	}

	public LocalConnection getOther() {
		return other;
	}

	public void setOther(LocalConnection other) {
		this.other = other;
	}

	public LocalConnection(Direction direction, Joinable joinable) {
		this.direction = direction;
		this.joinable = joinable;
	}

	public void join(LocalConnection other) {
		this.other = other;
		this.other.setOther(this);
	}
}
