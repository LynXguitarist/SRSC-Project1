package sssproxy;
/* hjUDPproxy, 20/Mar/18
 *
 * This is a very simple (transparent) UDP proxy
 * The proxy can listening on a remote source (server) UDP sender
 * and transparently forward received datagram packets in the
 * delivering endpoint
 *
 * Possible Remote listening endpoints:
 *    Unicast IP address and port: configurable in the file config.properties
 *    Multicast IP address and port: configurable in the code
 *  
 * Possible local listening endpoints:
 *    Unicast IP address and port
 *    Multicast IP address and port
 *       Both configurable in the file config.properties
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import sssocket.SSPSocket;

public class mySSPUDPProxy {
	
	private static final String PROP = "src/sssproxy/";
	private static final String CONFIG = "configs-examples/Phase-1/";

	public static void main(String[] args) throws Exception {
		InputStream inputStream = new FileInputStream(Paths.get(PROP + "config.properties").toAbsolutePath().toString());
		if (inputStream == null) {
			System.err.println("Configuration file not found!");
			System.exit(1);
		}

		// args[0] -> is a configuration file

		Properties properties = new Properties();
		properties.load(inputStream);
		String remote = properties.getProperty("remote");
		String destinations = properties.getProperty("localdelivery");

		SocketAddress inSocketAddress = parseSocketAddress(remote);
		Set<SocketAddress> outSocketAddressSet = Arrays.stream(destinations.split(",")).map(s -> parseSocketAddress(s))
				.collect(Collectors.toSet());

		SSPSocket inSocket = new SSPSocket(inSocketAddress, Paths.get(CONFIG + args[0]).toAbsolutePath().toString());
		SSPSocket outSocket = new SSPSocket();
		
		byte[] buffer = new byte[4 * 1024];
		while (true) {
			DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
			
			byte[] bytesToSend = inSocket.receivePacket(inPacket); // if remote is unicast

			System.out.print("*");
			for (SocketAddress outSocketAddress : outSocketAddressSet) {
				outSocket.sendClearText(new DatagramPacket(bytesToSend, bytesToSend.length, outSocketAddress)); 
			}
		}
	}

	private static InetSocketAddress parseSocketAddress(String socketAddress) {
		String[] split = socketAddress.split(":");
		String host = split[0];
		int port = Integer.parseInt(split[1]);
		return new InetSocketAddress(host, port);
	}
}
