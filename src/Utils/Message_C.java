package utils;

import java.io.Serializable;

public class Message_C implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private byte[] message_MP;
	private byte[] macedMessage;

	public Message_C(byte[] message_MP, byte[] macedMessage) {
		this.message_MP = message_MP;
		this.macedMessage = macedMessage;
	}

	public byte[] getMessage_MP() {
		return message_MP;
	}

	public void setMessage_MP(byte[] message_MP) {
		this.message_MP = message_MP;
	}

	public byte[] getMacedMessage() {
		return macedMessage;
	}

	public void setMacedMessage(byte[] macedMessage) {
		this.macedMessage = macedMessage;
	}

	
}
