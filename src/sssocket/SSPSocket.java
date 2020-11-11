package sssocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import utils .*;


public class SSPSocket extends DatagramSocket {

	private String ciphersuite, mac1, mac2, algorithm;
	private byte[] iv, session_key, mac1_key, mac2_key;
	private int session_key_size, mac1_key_size, mac2_key_size;

	private IvParameterSpec ivSpec;
	private Key secretKey, hMacKey, hMac2Key;
	private Cipher cipher;
	private Mac hMac, hMac2;

	public SSPSocket() throws SocketException {
		super();
	}

	public SSPSocket(String filename) throws Exception {
		super();
		init(filename);
	}

	public SSPSocket(SocketAddress inSocketAddress, String filename) throws Exception {
		super(inSocketAddress);
		init(filename);
	}

	/**
	 * Sends and encrypts packets
	 * 
	 * @throws IOException
	 */
	public void sendPacket(DatagramPacket packet) {

		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

			// SPP_PAYLOAD
			byte[] sspPayload = sspPayload(packet);

			// SSP_HEADER
			short versionInfo = 0x0101;
			byte contentType = 0x01;
			byte payloadType = 0x01;
			SSPHeader sspHeader = sspHeader(versionInfo, contentType, payloadType, sspPayload.length);
			byte[] sspHeader_bytes = Utils.convertToBytes(sspHeader);

			// SSPMessage
			SSPMessage sspMessage = new SSPMessage(sspHeader_bytes, sspPayload);
			byte[] sspMessage_bytes = Utils.convertToBytes(sspMessage);

			DatagramPacket outpacket = new DatagramPacket(sspMessage_bytes, sspMessage_bytes.length,
					packet.getSocketAddress());
			this.send(outpacket);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receives and decipher the packet
	 */
	public byte[] receivePacket(DatagramPacket packet) {
		byte[] outputBytes= null;
		try {
			this.receive(packet);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

			System.out.println(packet.getSocketAddress());

			// receives sspMessage -> sspHeader + sspPayload
			// Deals only with payload
			byte[] message = packet.getData();

			SSPMessage sspMessage = (SSPMessage) Utils.convertFromBytes(message);

			byte[] payload_bytes = sspMessage.getSspPayload();
			SSPPayload sspPayload = (SSPPayload) Utils.convertFromBytes(payload_bytes);
			byte[] C = sspPayload.getCipheredMessage();

			byte[] Mp_bytes = cipher.doFinal(C);
			Message_Mp mp = (Message_Mp) Utils.convertFromBytes(Mp_bytes);

			byte[] hMac_bytes = hMac.doFinal(Mp_bytes);
			byte[] payload_plaintext = mp.getPayloadPlainText();

			hMac.init(hMacKey);
			hMac.update(payload_plaintext, 0, payload_plaintext.length);

			if (!MessageDigest.isEqual(hMac.doFinal(), hMac_bytes)) {
				// message-integrity corrupted
				return outputBytes; // ou throw
			}
			outputBytes = payload_plaintext;
		} catch (IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException
				| InvalidKeyException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return outputBytes;
	}

	public void sendClearText(DatagramPacket packet) throws IOException {
		this.send(packet);
	}

	// ---------------------------------------PRIVATE_METHODS-------------------------------------------//

	private SSPHeader sspHeader(short versionInfo, byte contentType, byte payloadType, int payloadSize) {
		SSPHeader sspHeader = new SSPHeader(versionInfo, contentType, payloadType, payloadSize);
		return sspHeader;
	}

	/**
	 * Ks: symmetric session key || Km1: MAC key || Km2: a MAC key for Fast control
	 * Generate payload : E (Ks, [Mp || MAC1km1 (Mp) ]) || MAC2km2 (C)
	 * 
	 * @param packet
	 * @return
	 */
	private byte[] sspPayload(DatagramPacket packet) {

		byte[] payload = null;

		// MAC2km2 (C) -> cifra com MAC2 o C (MAC2.init(ENCRIPT_MODE))
		// MAC1km1 (Mp) -> cifra com MAC o Mp (MAC.init(ENCRIPT_MODE))
		// cipher.doFinal -> E ( Ks, [Mp || MAC1km1 (Mp) ] ) -> Mp com MAC1km1(Mp)
		// E (Ks, [Mp || MAC1km1 (Mp) ]) || MAC2km2 (C) :

		try {
			int id = 1;// ???????
			SecureRandom nonce = new SecureRandom();
			byte[] M = packet.getData();

			// FAZER PRIVATE METHODS
			// PORQUE MAC PODE VIR NULL

			// Mp = [id || nonce || M]
			Message_Mp Mp = new Message_Mp(id, nonce, M);
			byte[] Mp_bytes = Utils.convertToBytes(Mp);

			// [ Mp || MACKM (Mp) ]
			byte[] hMac_bytes = hMac.doFinal(Mp_bytes);

			// C = E (KS, [ Mp || MACKM (Mp) ] )
			byte[] C = cipher.doFinal(hMac_bytes, 0, hMac.getMacLength()); // C

			payload = hMac2.doFinal(C);
		} catch (IllegalBlockSizeException | BadPaddingException | IllegalStateException | IOException e) {
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

		// IV, KEY, SESSION_KEY, CIPHER_SUITE
		if (this.iv != null)
			ivSpec = new IvParameterSpec(this.iv);

		secretKey = new SecretKeySpec(this.session_key, this.algorithm);
		cipher = Cipher.getInstance(this.ciphersuite);

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
			hMac2Key = new SecretKeySpec(this.mac2_key, this.mac2);
			hMac2.init(hMac2Key);
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
			this.iv = Utils.convertHexToByte(iv_content.split(","));

		String session_content = in.readLine().split(":")[1];
		if (!session_content.toUpperCase().equals("NULL")) {
			this.session_key_size = Integer.parseInt(session_content); // sessionKeySize
			// loops the content until length of array = sessionKeySize
			this.session_key = new byte[this.session_key_size];
			int session_key_length = 0;
			while (session_key_length != this.session_key_size) {
				String content = in.readLine().trim();
				if (content.contains(":")) // if is the first line, splits the bytes from the text
					content = content.split(":")[1];

				byte[] contents_in_bytes = Utils.convertHexToByte(content.split(","));
				// copies the byte content into the array
				System.arraycopy(contents_in_bytes, 0, this.session_key, session_key_length, contents_in_bytes.length);
				session_key_length += contents_in_bytes.length;
			}
		} else {
			in.readLine();
		}
		// mac1KeySize, if it is null doesn't read the next line
		String mac1_key_content = in.readLine().split(":")[1];
		if (!mac1_key_content.toUpperCase().equals("NULL")) {
			this.mac1_key_size = Integer.parseInt(mac1_key_content);
			// mac1Key
			this.mac1_key = Utils.convertHexToByte(in.readLine().split(":")[1].split(","));
		} else {
			in.readLine();
		}
		// mac2KeySize, if it is null doesn't read the next line
		String mac2_key_content = in.readLine().split(":")[1];
		if (!mac2_key_content.toUpperCase().equals("NULL")) {
			this.mac2_key_size = Integer.parseInt(mac2_key_content);
			// mac2Key
			this.mac2_key = Utils.convertHexToByte(in.readLine().split(":")[1].split(","));
		}

		// get algorithm
		this.algorithm = this.ciphersuite.split("/")[0];
		in.close();
	}

}
