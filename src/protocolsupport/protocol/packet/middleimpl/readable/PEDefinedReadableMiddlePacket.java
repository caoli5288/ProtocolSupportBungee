package protocolsupport.protocol.packet.middleimpl.readable;

import io.netty.buffer.ByteBuf;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;

public abstract class PEDefinedReadableMiddlePacket extends DefinedReadableMiddlePacket {

	public PEDefinedReadableMiddlePacket(int packetId) {
		super(packetId);
	}

	@Override
	protected void writePacketId(ByteBuf to) {
		PEPacketIdSerializer.writePacketId(connection.getVersion(), to, packetId);
	}

}
