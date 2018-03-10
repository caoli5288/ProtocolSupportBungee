package protocolsupport.protocol.packet.middleimpl.readable.handshake.v_4_5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.StatusRequest;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middleimpl.readable.LegacyDefinedReadableMiddlePacket;

import java.util.Arrays;
import java.util.Collection;

public class PingHandshakePacket extends LegacyDefinedReadableMiddlePacket {

	public static final int PACKET_ID = 0xFE;

	public PingHandshakePacket() {
		super(PACKET_ID);
	}

	@Override
	protected void read0(ByteBuf from) {
		System.out.println("Reading 1.5/1.4 ping!");
		from.readUnsignedByte();
	}

	@Override
	public Collection<PacketWrapper> toNative() {
		return Arrays.asList(
				new PacketWrapper(new Handshake(ProtocolVersion.MINECRAFT_1_7_10.getId(), "127.0.0.1", 25565, 1), Unpooled.EMPTY_BUFFER),
				new PacketWrapper(new StatusRequest(), Unpooled.wrappedBuffer(readbytes))
		);
	}

}
