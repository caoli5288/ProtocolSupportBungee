package protocolsupport.protocol.pipeline.version.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.ReadableMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.BossEventPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.FromServerChatPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.FromServerPluginMessagePacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.KickPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.LoginPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.SpawnEntity;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.SpawnPlayer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.storage.NetworkDataCache;
import protocolsupport.protocol.utils.registry.PacketIdMiddleTransformerRegistry;

import java.util.List;

public class FromServerPacketDecoder extends MinecraftDecoder {

	protected final PacketIdMiddleTransformerRegistry<ReadableMiddlePacket> registry = new PacketIdMiddleTransformerRegistry<>();
	{
		registry.register(Protocol.GAME, BossEventPacket.PACKET_ID, BossEventPacket.class);
		registry.register(Protocol.GAME, SpawnEntity.PACKET_ID, SpawnEntity.class);
		registry.register(Protocol.GAME, SpawnPlayer.PACKET_ID, SpawnPlayer.class);
		registry.register(Protocol.GAME, KickPacket.PACKET_ID, KickPacket.class);
		registry.register(Protocol.GAME, LoginPacket.PACKET_ID, LoginPacket.class);
		registry.register(Protocol.GAME, FromServerChatPacket.PACKET_ID, FromServerChatPacket.class);
		registry.register(Protocol.GAME, FromServerPluginMessagePacket.PACKET_ID, FromServerPluginMessagePacket.class);
//		registry.register(Protocol.GAME, RespawnPacket.PACKET_ID, RespawnPacket.class); //TODO: implement after implementing in PSPE
//		registry.register(Protocol.GAME, PlayerListItemPacket.PACKET_ID, PlayerListItemPacket.class); //TODO: implement at bungee level (without this entry it's a direct passthrough, so entries will duplicate upon server switch)
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
			packets.add(new PacketWrapper(null, buf.copy()));
		} else {
			transformer.read(buf);
			if (buf.isReadable()) {
				throw new DecoderException("Did not read all data from packet " + transformer.getClass().getName() + ", bytes left: " + buf.readableBytes());
			}
			packets.addAll(transformer.toNative());
		}
	}

	protected int readPacketId(ByteBuf from) {
		int id = VarNumberSerializer.readVarInt(from);
		from.readByte();
		from.readByte();
		return id;
	}

}
