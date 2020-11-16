package utils;

import java.io.Serializable;

public class SSPPayload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private byte[] cipheredMessage;
	private byte[] macedMessage;

	public SSPPayload(byte[] cipheredMessage, byte[] macedMessage) {
		this.cipheredMessage = cipheredMessage;
		this.macedMessage = macedMessage;
	}

	public byte[] getCipheredMessage() {
		return cipheredMessage;
	}

	public void setCipheredMessage(byte[] cipheredMessage) {
		this.cipheredMessage = cipheredMessage;
	}

	public byte[] getMacMessage() {
		return macedMessage;
	}

	public void setMacedMessage(byte[] macedMessage) {
		this.macedMessage = macedMessage;
	}

}
