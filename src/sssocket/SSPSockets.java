package sssocket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class SSPSockets extends DatagramSocket {

	// Receives as input the crypto to use
	public SSPSockets() throws SocketException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sends and encrypts packets
	 */
	public void send(DatagramPacket packet) {

	}

	/**
	 * Receives and decipher the packet
	 */
	public void receive(DatagramPacket packet) {

	}

}
