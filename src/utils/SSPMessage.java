package utils;

import java.io.Serializable;

public class SSPMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private byte[] sspHeader, sspPayload;
	
	public SSPMessage(byte[]sspHeader, byte[] sspPayload) {
		this.sspHeader = sspHeader;
		this.sspPayload = sspPayload;
	}

	public byte[] getSspHeader() {
		return sspHeader;
	}

	public void setSspHeader(byte[] sspHeader) {
		this.sspHeader = sspHeader;
	}

	public byte[] getSspPayload() {
		return sspPayload;
	}

	public void setSspPayload(byte[] sspPayload) {
		this.sspPayload = sspPayload;
	}

}
