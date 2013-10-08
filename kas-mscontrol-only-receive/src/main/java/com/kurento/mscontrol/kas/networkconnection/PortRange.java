package com.kurento.mscontrol.kas.networkconnection;

/**
 * PortRange class represents a valid interval of network ports.
 */
public class PortRange {

	private int minPort;
	private int maxPort;

	/**
	 * Constructs a PortRange object to represent a port range between minPort
	 * and maxPort. The minPort and the maxPort are considered inclusive.
	 * 
	 * @param minPort
	 *            range min port. It must be greater than or equal 1024.
	 * @param maxPort
	 *            range max port. It must be greater than minPort and less than
	 *            or equal 65535.
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if minPort is less than 1024.</li>
	 *             <li>if maxPort is less than minPort or greater than than
	 *             65535.</li>
	 *             </ul>
	 */
	public PortRange(int minPort, int maxPort) throws IllegalArgumentException {
		if (minPort < 1024)
			throw new IllegalArgumentException(
					"minPort must be greater than or equal 1");
		if ((maxPort < minPort) || (maxPort > 65535))
			throw new IllegalArgumentException(
					"maxPort must be greater than minPort and less than or equal 65535");
		this.minPort = minPort;
		this.maxPort = maxPort;
	}

	public int getMinPort() {
		return minPort;
	}

	public int getMaxPort() {
		return maxPort;
	}
}
