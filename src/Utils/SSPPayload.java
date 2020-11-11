package Utils;

import java.io.Serializable;

public class SSPPayload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private byte[] cipheredMessage;
	private byte[] mac;

	public SSPPayload(byte[] cipheredMessage, byte[] mac) {
		this.cipheredMessage = cipheredMessage;
		this.mac = mac;
	}

	public byte[] getCipheredMessage() {
		return cipheredMessage;
	}

	public void setCipheredMessage(byte[] cipheredMessage) {
		this.cipheredMessage = cipheredMessage;
	}

	public byte[] getMac() {
		return mac;
	}

	public void setMac(byte[] mac) {
		this.mac = mac;
	}

}
