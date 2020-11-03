package Utils;

import java.security.SecureRandom;

public class Message_Mp {

	private int id;
	private SecureRandom nonce;
	private byte[] payloadPlainText;

	public Message_Mp(int id, SecureRandom nonce, byte[] payloadPlainText) {
		this.id = id;
		this.nonce = nonce;
		this.payloadPlainText = payloadPlainText;
	}

	public int getId() {
		return id;
	}

	public SecureRandom getNonce() {
		return nonce;
	}

	public byte[] getPayloadPlainText() {
		return payloadPlainText;
	}

}
