package protocolsupport.protocol.pipeline.version.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.ReadableMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServBossEventPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServerChatPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServerItemEntityAddPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServerPluginMessagePacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServerEntityRemovePacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServerEntityAddPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.KickPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.LoginPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.PlayerListItemPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.RespawnPacket;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;
import protocolsupport.protocol.storage.NetworkDataCache;
import protocolsupport.protocol.utils.registry.PacketIdMiddleTransformerRegistry;

import java.util.List;

public class FromServerPacketDecoder extends MinecraftDecoder {

	protected final PacketIdMiddleTransformerRegistry<ReadableMiddlePacket> registry = new PacketIdMiddleTransformerRegistry<>();
	{
		registry.register(Protocol.GAME, FromServBossEventPacket.PACKET_ID, FromServBossEventPacket.class);
		registry.register(Protocol.GAME, KickPacket.PACKET_ID, KickPacket.class);
		registry.register(Protocol.GAME, LoginPacket.PACKET_ID, LoginPacket.class);
		registry.register(Protocol.GAME, FromServerChatPacket.PACKET_ID, FromServerChatPacket.class);
		registry.register(Protocol.GAME, RespawnPacket.PACKET_ID, RespawnPacket.class);
		registry.register(Protocol.GAME, PlayerListItemPacket.PACKET_ID, PlayerListItemPacket.class);
		registry.register(Protocol.GAME, FromServerPluginMessagePacket.PACKET_ID, FromServerPluginMessagePacket.class);
		registry.register(Protocol.GAME, FromServerEntityAddPacket.PACKET_ID, FromServerEntityAddPacket.class);
		registry.register(Protocol.GAME, FromServerEntityRemovePacket.PACKET_ID, FromServerEntityRemovePacket.class);
		registry.register(Protocol.GAME, FromServerItemEntityAddPacket.PACKET_ID, FromServerItemEntityAddPacket.class);
	}

	protected final Connection connection;
	protected final NetworkDataCache cache;

	public FromServerPacketDecoder(Connection connection, NetworkDataCache cache) {
		super(Protocol.GAME, false, ProtocolVersion.MINECRAFT_1_7_10.getId());
		this.connection = connection;
		this.cache = cache;
		registry.setCallBack(transformer -> {
			transformer.setConnection(this.connection);
			transformer.setSharedStorage(this.cache);
		});
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> packets) throws Exception {
		if (!buf.isReadable()) {
			return;
		}
		buf.markReaderIndex();
		ReadableMiddlePacket transformer = registry.getTransformer(Protocol.GAME, readPacketId(buf), false);
		if (transformer == null) {
			buf.resetReaderIndex();
			packets.add(new PacketWrapper(null, buf.retain()));
		} else {
			transformer.read(buf);
			if (buf.isReadable()) {
				throw new DecoderException("Did not read all data from packet " + transformer.getClass().getName() + ", bytes left: " + buf.readableBytes());
			}
			packets.addAll(transformer.toNative());
		}
	}

	protected int readPacketId(ByteBuf from) {
		return PEPacketIdSerializer.readPacketId(connection.getVersion(), from);
	}

}
