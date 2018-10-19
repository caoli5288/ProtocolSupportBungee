package protocolsupport.protocol.pipeline.version.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.injector.pe.PEProxyServerInfoHandler;
import protocolsupport.protocol.packet.middle.ReadableMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.readable.handshake.v_pe.LoginHandshakePacket;
import protocolsupport.protocol.packet.middleimpl.readable.handshake.v_pe.PingHandshakePacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.CommandRequestPacket;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromClientChatPacket;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;
import protocolsupport.protocol.storage.NetworkDataCache;
import protocolsupport.protocol.utils.registry.PacketIdMiddleTransformerRegistry;

import java.util.List;

public class FromClientPacketDecoder extends MinecraftDecoder {

	protected final PacketIdMiddleTransformerRegistry<ReadableMiddlePacket> registry = new PacketIdMiddleTransformerRegistry<>();
	{
		registry.register(Protocol.HANDSHAKE, LoginHandshakePacket.PACKET_ID, LoginHandshakePacket.class);
		registry.register(Protocol.HANDSHAKE, PEProxyServerInfoHandler.PACKET_ID, PingHandshakePacket.class);
		registry.register(Protocol.GAME, FromClientChatPacket.PACKET_ID, FromClientChatPacket.class);
		registry.register(Protocol.GAME, CommandRequestPacket.PACKET_ID, CommandRequestPacket.class);
	}

	protected final Connection connection;
	protected final NetworkDataCache cache;

	protected Protocol protocol = Protocol.HANDSHAKE;

	public FromClientPacketDecoder(Connection connection, NetworkDataCache cache) {
		super(Protocol.HANDSHAKE, true, ProtocolVersion.MINECRAFT_1_7_10.getId());
		this.connection = connection;
		this.cache = cache;
		registry.setCallBack(transformer -> {
			transformer.setConnection(this.connection);
			transformer.setSharedStorage(this.cache);
		});
	}

	@Override
	public void setProtocol(Protocol protocol) {
		super.setProtocol(protocol);
		this.protocol = protocol;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> packets) throws Exception {
		if (!buf.isReadable()) {
			return;
		}
		buf.markReaderIndex();
		ReadableMiddlePacket transformer = registry.getTransformer(protocol, PEPacketIdSerializer.readPacketId(connection.getVersion(), buf), false);
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

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {// catch authlib's exception
		if (ctx.channel().isActive()) {
			ProxyServer.getInstance().getLogger().warning("Exception in from client packet decoder " + cause);
			ctx.close();
		}
	}
}
