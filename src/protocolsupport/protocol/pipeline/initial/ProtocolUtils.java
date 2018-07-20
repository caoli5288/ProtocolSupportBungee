package protocolsupport.protocol.pipeline.initial;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.utils.ProtocolVersionsHelper;

public class ProtocolUtils {

	protected static ProtocolVersion get16PingVersion(int protocolId) {
		switch (protocolId) {
			case 78: {
				return ProtocolVersion.MINECRAFT_1_6_4;
			}
			case 74: {
				return ProtocolVersion.MINECRAFT_1_6_2;
			}
			case 73: {
				return ProtocolVersion.MINECRAFT_1_6_1;
			}
			default: {
				return ProtocolVersion.MINECRAFT_1_6_4;
			}
		}
	}

	protected static ProtocolVersion readOldHandshake(ByteBuf data) {
		return ProtocolVersionsHelper.getOldProtocolVersion(data.readUnsignedByte());
	}

	protected static ProtocolVersion readNewHandshake(ByteBuf data) {
		int packetId = VarNumberSerializer.readVarInt(data);
		if (packetId == 0x00) {
			return ProtocolVersionsHelper.getNewProtocolVersion(VarNumberSerializer.readVarInt(data));
		} else {
			throw new DecoderException(packetId + " is not a valid packet id");
		}
	}

	protected static ProtocolVersion readPEHandshake(ByteBuf data) {
		data.readByte();
		data.readByte();
		int incomingversion = data.readInt();
		ProtocolVersion version = ProtocolVersion.fromId(incomingversion);
		if (version == ProtocolVersion.UNKNOWN) {
			if (incomingversion > ProtocolVersion.getLatest(ProtocolType.PE).getId()) {
				return ProtocolVersion.MINECRAFT_PE_FUTURE;
			}
			return ProtocolVersion.MINECRAFT_PE_LEGACY;
		}
		return version;
	}

}
