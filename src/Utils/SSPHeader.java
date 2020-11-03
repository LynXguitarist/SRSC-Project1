package Utils;

public class SSPHeader {

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
