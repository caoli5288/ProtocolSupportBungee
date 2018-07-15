package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.Chat;
import protocolsupport.protocol.packet.middleimpl.writeable.PESingleWriteablePacket;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

public class ToClientChatPacket extends PESingleWriteablePacket<Chat> {

	public ToClientChatPacket() {
		super(9);
	}

	@Override
	protected void write(ByteBuf data, Chat packet) {
		data.writeByte(packet.getPosition() == 2 ? 5 : 0); //type
		data.writeByte(0); //isLocalise?
		StringSerializer.writeVarIntUTF8String(data, ComponentSerializer.parse(packet.getMessage())[0].toLegacyText());
		StringSerializer.writeVarIntUTF8String(data, ""); //Xbox user ID
		StringSerializer.writeVarIntUTF8String(data, ""); //Platform Chat ID
	}

}
