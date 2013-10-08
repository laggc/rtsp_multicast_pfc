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

package com.kurento.mscontrol.kas.networkconnection;

/**
 * NetIF indicate the network interface.
 */
public enum NetIF {
	WIFI(3000000), MOBILE(384000);

	public static final int MIN_BANDWITH = 50000;
	private int maxBandwidth;

	public int getMaxBandwidth() {
		return maxBandwidth;
	}

	private NetIF(int maxBandwidth) {
		this.maxBandwidth = maxBandwidth;
	}
}
