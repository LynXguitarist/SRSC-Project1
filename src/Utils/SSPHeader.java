package Utils;

import java.io.Serializable;

public class SSPHeader implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private short versionInfo;
	private byte contentType, payloadType;
	private int payloadSize;

	public SSPHeader(short versionInfo, byte contentType, byte payloadType, int payloadSize) {
		this.versionInfo = versionInfo;
		this.contentType = contentType;
		this.payloadType = payloadType;
		this.payloadSize = payloadSize;
	}

	public short getVersionInfo() {
		return versionInfo;
	}

	public byte getContentType() {
		return contentType;
	}

	public byte getPayloadType() {
		return payloadType;
	}

	public int getPayloadSize() {
		return payloadSize;
	}
}
