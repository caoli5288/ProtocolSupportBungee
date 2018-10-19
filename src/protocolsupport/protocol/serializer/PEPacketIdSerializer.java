package protocolsupport.protocol.serializer;

import io.netty.buffer.ByteBuf;
import protocolsupport.api.ProtocolVersion;

public class PEPacketIdSerializer {

	public static int readPacketId(ProtocolVersion version, ByteBuf from) {
		int id = VarNumberSerializer.readVarInt(from);
		if (version.isAfterOrEq(ProtocolVersion.MINECRAFT_PE_1_6)) {
			return id;
		}
		from.readByte();
		from.readByte();
		return id;
	}

	public static void writePacketId(ProtocolVersion version, ByteBuf data, int packetId) {
		VarNumberSerializer.writeVarInt(data, packetId);
		if (version.isAfterOrEq(ProtocolVersion.MINECRAFT_PE_1_6)) {
			return;
		}
		data.writeByte(0);
		data.writeByte(0);
	}

}
