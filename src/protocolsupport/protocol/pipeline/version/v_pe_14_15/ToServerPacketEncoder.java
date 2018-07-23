package protocolsupport.protocol.pipeline.version.v_pe_14_15;

import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientStatus;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.PluginMessage;
import protocolsupport.api.Connection;
import protocolsupport.protocol.packet.middleimpl.writeable.NoopWriteablePacket;
import protocolsupport.protocol.packet.middleimpl.writeable.handshake.v_4_5_6_pe.HandshakeCachePacket;
import protocolsupport.protocol.packet.middleimpl.writeable.handshake.v_pe.LoginRequestServerHandshakePacket;
import protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe.ToServerChatPacket;
import protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe.ToServerPluginMessagePacket;
import protocolsupport.protocol.pipeline.version.AbstractPacketEncoder;
import protocolsupport.protocol.storage.NetworkDataCache;

public class ToServerPacketEncoder extends AbstractPacketEncoder {

	{
		registry.register(Handshake.class, HandshakeCachePacket.class);
		registry.register(LoginRequest.class, LoginRequestServerHandshakePacket.class);
		registry.register(KeepAlive.class, NoopWriteablePacket.class);
		registry.register(Chat.class, ToServerChatPacket.class);
		registry.register(PluginMessage.class, ToServerPluginMessagePacket.class);
		registry.register(ClientStatus.class, NoopWriteablePacket.class);
	}

	public ToServerPacketEncoder(Connection connection, NetworkDataCache cache) {
		super(connection, cache);
	}

}
