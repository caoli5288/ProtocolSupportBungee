package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.Chat;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromClientChatPacket;
import protocolsupport.protocol.packet.middleimpl.writeable.PESingleWriteablePacket;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

public class ToServerChatPacket extends PESingleWriteablePacket<Chat> {

	public ToServerChatPacket() {
		super(9);
	}

	@Override
	protected void write(ByteBuf data, Chat packet) {
		data.writeByte(FromClientChatPacket.CLIENT_CHAT_TYPE); //type
		data.writeBoolean(true); //isLocalise?
		StringSerializer.writeVarIntUTF8String(data, ""); //sender username
		if (connection.getVersion().isBefore(ProtocolVersion.MINECRAFT_PE_1_7)) {
			StringSerializer.writeVarIntUTF8String(data, ""); //third party name
			VarNumberSerializer.writeSVarInt(data, 1); //source platform
		}
		StringSerializer.writeVarIntUTF8String(data, packet.getMessage());
		StringSerializer.writeVarIntUTF8String(data, ""); //Xbox user ID
		StringSerializer.writeVarIntUTF8String(data, ""); //platform chat ID
	}

}
