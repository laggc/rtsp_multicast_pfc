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

import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.join.Joinable;

public class JoinableImpl implements Joinable {


	private ArrayList<LocalConnection> connections = new ArrayList<LocalConnection>();

	protected synchronized void addConnection(LocalConnection conn) {
		this.connections.add(conn);
	}

	protected synchronized void removeConnection(LocalConnection conn) {
		this.connections.remove(conn);
	}

	@Override
	public synchronized Joinable[] getJoinees() throws MsControlException {
		int i = 0;
		Joinable[] joinees = new Joinable[connections.size()];

		for (LocalConnection connection : connections)
			joinees[i++] = connection.getJoinable();

		return joinees;
	}

	@Override
	public synchronized Joinable[] getJoinees(Direction direction) throws MsControlException {
		int i = 0;
		Joinable[] joinees = new Joinable[connections.size()];

		for (LocalConnection connection : connections) {
			if (connection.getDirection().equals(direction)
					|| connection.getDirection().equals(Direction.DUPLEX)) {
				joinees[i++] = connection.getJoinable();
			}
		}

		return joinees;
	}

	@Override
	public synchronized void join(Direction direction, Joinable other) throws MsControlException {
		if (other == null)
			throw new MsControlException("other is null.");

		// Search old join with other
		LocalConnection connection = null;
		for (LocalConnection conn : connections) {
			if (conn.getJoinable().equals(other)) {
				connection = conn;
				break;
			}
		}

		if (connection != null) {// Delete join to re-join
			((JoinableImpl) other).removeConnection(connection.getOther());
			this.connections.remove(connection);
		}

		// join
		LocalConnection connection1 = new LocalConnection(direction, other);

		Direction dir2 = Direction.DUPLEX;
		if (Direction.SEND.equals(direction))
			dir2 = Direction.RECV;
		else if (Direction.RECV.equals(direction))
			dir2 = Direction.SEND;

		LocalConnection connection2 = new LocalConnection(dir2, this);

		connection1.join(connection2);

		this.connections.add(connection1);
		((JoinableImpl) other).addConnection(connection2);
	}

	@Override
	public synchronized void unjoin(Joinable other) throws MsControlException {
		LocalConnection connection = null;
		for (LocalConnection conn : connections) {
			if (conn.getJoinable().equals(other)) {
				connection = conn;
				break;
			}
		}

		if (connection == null)
			throw new MsControlException("No connected: " + other);

		((JoinableImpl) other).removeConnection(connection.getOther());
		this.connections.remove(connection);
	}

}
