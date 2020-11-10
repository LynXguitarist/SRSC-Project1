package ssstream;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;

import sssocket.SSPSocket;

public class mySSPStreamServer {

	private static final String MOVIES = "movies/";
	private static final String CONFIG = "configs-examples/Phase-1/";

	static public void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Erro, usar: mySend <movie> <ip-multicast-address> <port> <file>");
			System.out.println("        or: mySend <movie> <ip-unicast-address> <port> <file>");
			System.exit(-1);
		}
		// args[3] -> is a configuration file (text file) for the SSP protocol

		int size;
		int count = 0;
		long time;
		
		String movie_file = Paths.get(MOVIES + args[0]).toAbsolutePath().toString();
		DataInputStream g = new DataInputStream(new FileInputStream(movie_file));
		
		byte[] buff = new byte[4096];

		String config_file = Paths.get(CONFIG + args[3]).toAbsolutePath().toString();
		SSPSocket s = new SSPSocket(config_file); // mudar pelo SSPSocket

		InetSocketAddress addr = new InetSocketAddress(args[1], Integer.parseInt(args[2]));
		DatagramPacket p = new DatagramPacket(buff, buff.length, addr);
		long t0 = System.nanoTime(); // tempo de referencia para este processo
		long q0 = 0;

		while (g.available() > 0) {
			size = g.readShort();
			time = g.readLong();
			if (count == 0)
				q0 = time; // tempo de referencia no stream
			count += 1;
			g.readFully(buff, 0, size);
			p.setData(buff, 0, size);
			p.setSocketAddress(addr);
			long t = System.nanoTime();
			Thread.sleep(Math.max(0, ((time - q0) - (t - t0)) / 1000000));

			s.sendPacket(p);// mudar pelo SSPSocket
			System.out.print(".");
		}

		System.out.println("DONE! all frames sent: " + count);
	}

}
