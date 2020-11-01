package sssocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SSPSockets extends DatagramSocket {

	private String ciphersuite, mac1, mac2, algorithm;
	private byte[] iv, session_key, mac1_key, mac2_key;
	private int session_key_size, mac1_key_size, mac2_key_size;

	private IvParameterSpec ivSpec;
	private Key secretKey, hMacKey, hMacKey2;
	private Cipher cipher;
	private Mac hMac, hMac2;

	// Receives as input the file with the crypto to use
	public SSPSockets(String filename) throws Exception {
		super();
		init(filename);
	}

	/**
	 * Sends and encrypts packets
	 */
	public void send(DatagramPacket packet) {
		// SSP_HEADER
		short versionInfo = 0x0101;
		byte contentType = 0x01;
		byte payloadType = 0x01;

		// SPP_PAYLOAD
		byte[] sspPayload = sspPayload(packet);

		String message = sspHeader(versionInfo, contentType, payloadType, sspPayload.length) + "||" + sspPayload;
		byte[] sppMessage = message.getBytes();

		DatagramPacket outpacket = new DatagramPacket(sppMessage, sppMessage.length);
		this.send(outpacket);
	}

	/**
	 * Receives and decipher the packet
	 */
	public void receive(DatagramPacket packet) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			byte[] inputBytes = cipher.doFinal(packet.getData());

			// receives sspMessage -> sspHeader + sspPayload
			// Deals only with payload

			DatagramPacket inpacket = new DatagramPacket(inputBytes, inputBytes.length);
			this.receive(inpacket);
		} catch (IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException
				| InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------------PRIVATE_METHODS-------------------------------------------//

	private String sspHeader(short versionInfo, byte contentType, byte payloadType, int payloadSize) {
		return versionInfo + "||" + contentType + "||" + payloadType + "||" + payloadSize;
	}

	private byte[] sspPayload(DatagramPacket packet) {
		byte[] payload = null;
		// Generate payload E (Ks, [Mp || MAC1km1 (Mp) ]) || MAC2km2 (C)
		// Ks: symmetric session key
		// Km1: MAC key
		// Km2: a MAC key for Fast control DoS mitigation
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

			// Mp = [id || nonce || M]
			int id = 0;// ???????
			SecureRandom nonce = new SecureRandom();
			byte[] M = packet.getData();

			// C = E (KS, [ Mp || MACKM (Mp) ]
			
			payload = cipher.doFinal(packet.getData());
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}
		return payload;

	}

	/**
	 * Init the socket
	 * 
	 * @param filename
	 */
	private void init(String filename) throws Exception {
		processFile(filename);

		// IV, KEY, SESSION_KEY,CIPHER_SUITE
		if (this.iv != null)
			ivSpec = new IvParameterSpec(this.iv);

		secretKey = new SecretKeySpec(this.session_key, this.algorithm);
		cipher = Cipher.getInstance(this.ciphersuite);

		// NAO SEI SE E PRECISO??!?!?

		// MAC1
		if (this.mac1 != null) {
			hMac = Mac.getInstance(this.mac1);
			// hMacKey = new SecretKeySpec(secretKey.getEncoded(), this.mac1);
			hMacKey = new SecretKeySpec(this.mac1_key, this.mac1);
			hMac.init(hMacKey);
		}
		// MAC2
		if (this.mac2 != null) {
			hMac2 = Mac.getInstance(this.mac2);
			// hMacKey2 = new SecretKeySpec(secretKey.getEncoded(), this.mac2);
			hMacKey = new SecretKeySpec(this.mac2_key, this.mac1);
			hMac2.init(hMacKey2);
		}

	}

	/**
	 * gets all the info from the file received as param
	 * 
	 * @param filename
	 */
	private void processFile(String filename) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(Paths.get(filename).toString()));
		// Sets variables, keys, ciphers, etc
		this.ciphersuite = in.readLine().split(":")[1]; // ciphersuite
		this.mac1 = in.readLine().split(":")[1]; // mac1_ciphersuite
		this.mac2 = in.readLine().split(":")[1]; // mac2_ciphersuite
		// iv
		String iv_content = in.readLine().split(":")[1];
		if (!iv_content.toUpperCase().equals("NULL"))
			this.iv = iv_content.getBytes(",");

		String session_content = in.readLine().split(":")[1];
		if (!session_content.toUpperCase().equals("NULL")) {
			this.session_key_size = Integer.parseInt(session_content); // sessionKeySize
			// loops the content unitl length of array = sessionKeySize
			this.session_key = new byte[this.session_key_size];
			while (this.session_key.length != this.session_key_size) {
				String content = in.readLine().trim();
				if (content.contains(":")) // if is the first line, splits the bytes from the text
					content = content.split(":")[1];

				byte[] contents_in_bytes = content.getBytes(",");
				// copies the byte content into the array
				System.arraycopy(contents_in_bytes, 0, this.session_key, 0, contents_in_bytes.length);
			}
		} else {
			in.readLine();
		}
		// mac1KeySize, if it is null doesn't read the next line
		String mac1_key_content = in.readLine().split(":")[1];
		if (!mac1_key_content.toUpperCase().equals("NULL")) {
			this.mac1_key_size = Integer.parseInt(mac1_key_content);
			// mac1Key
			this.mac1_key = in.readLine().split(":")[1].getBytes(",");
		} else {
			in.readLine();
		}
		// mac2KeySize, if it is null doesn't read the next line
		String mac2_key_content = in.readLine().split(":")[1];
		if (!mac2_key_content.toUpperCase().equals("NULL")) {
			this.mac2_key_size = Integer.parseInt(mac2_key_content);
			// mac2Key
			this.mac2_key = in.readLine().split(":")[1].getBytes(",");
		}

		// get algorithm
		this.algorithm = this.ciphersuite.split("/")[0];
		in.close();

	}

}
