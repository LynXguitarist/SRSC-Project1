package sssocket;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SSPSockets extends DatagramSocket {

	private static final String CONFIG_PATH = "D:\\FCT\\SRSC\\PRATICA\\PROJECTOS\\PROJECTO1\\SRSC-Project1\\configs-examples\\Phase-1";

	private String ciphersuite, mac1, mac2, algorithm;
	private byte[] iv, session_key, mac1_key, mac2_key;
	private int session_key_size, mac1_key_size, mac2_key_size;

	private IvParameterSpec ivSpec;
	private Key secretKey, hMacKey, hMacKey2;
	private Cipher cipher;
	private Mac hMac, hMac2;

	// Receives as input the file with the crypto to use
	public SSPSockets(String filename) throws SocketException {
		super();
		init(filename);
	}

	/**
	 * Sends and encrypts packets
	 */
	public void send(DatagramPacket packet) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] outputBytes = cipher.doFinal(packet.getData());
			// generate payload E (KS, [ Mp || MAC1Km1 (Mp) ] ) || MAC2Km2 (C)
			/**
			 * C = E (KS, [ Mp || MACKM (Mp) ] 
			 * Ks: symmetric session key 
			 * Km1: MAC key 
			 * Km2: a MAC key for Fast control DoS mitigation
			 */

			DatagramPacket outpacket = new DatagramPacket(outputBytes, outputBytes.length);
			this.send(outpacket);
		} catch (IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException
				| InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Receives and decipher the packet
	 */
	public void receive(DatagramPacket packet) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			byte[] inputBytes = cipher.doFinal(packet.getData());
			// generate payload E (KS, [ Mp || MAC1Km1 (Mp) ] ) || MAC2Km2 (C)
			/**
			 * C = E (KS, [ Mp || MACKM (Mp) ] 
			 * Ks: symmetric session key 
			 * Km1: MAC key 
			 * Km2: a MAC key for Fast control DoS mitigation
			 */

			DatagramPacket inpacket = new DatagramPacket(inputBytes, inputBytes.length);
			this.receive(inpacket);
		} catch (IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException
				| InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Init the socket
	 * 
	 * @param filename
	 */
	private void init(String filename) {
		processFile(filename);
		try {
			// IV, KEY, SESSION_KEY,CIPHER_SUITE
			ivSpec = new IvParameterSpec(this.iv);
			secretKey = new SecretKeySpec(this.session_key, this.algorithm);
			cipher = Cipher.getInstance(this.ciphersuite);

			// MAC1
			hMac = Mac.getInstance(this.mac1);
			hMacKey = new SecretKeySpec(secretKey.getEncoded(), this.mac1);

			// MAC2
			hMac2 = Mac.getInstance(this.mac2);
			hMacKey2 = new SecretKeySpec(secretKey.getEncoded(), this.mac2);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * gets all the info from the file received as param
	 * 
	 * @param filename
	 */
	private void processFile(String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(CONFIG_PATH + filename));
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
					String content = in.readLine();
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
