package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.Validate;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Chat;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

public class FromClientChatPacket extends PEDefinedReadableMiddlePacket {

	public static final int PACKET_ID = 9;

	public static final int CLIENT_CHAT_TYPE = 1;

	protected String message;

	public FromClientChatPacket() {
		super(PACKET_ID);
	}

	@Override
	protected void read0(ByteBuf from) {
		int type = from.readUnsignedByte();
		Validate.isTrue(type == CLIENT_CHAT_TYPE, MessageFormat.format("Unexcepted serverbound chat type, expected {0}, but received {1}", CLIENT_CHAT_TYPE, type));
		from.readBoolean(); //needs translation
		MiscSerializer.nullVarArray(from); //skip sender
		MiscSerializer.nullVarArray(from); //skip third party name
		VarNumberSerializer.readSVarInt(from); //skip source platform
		message = StringSerializer.readVarIntUTF8String(from);
		MiscSerializer.nullVarArray(from); //skip Xbox user ID
		MiscSerializer.nullVarArray(from); //skip platform chat ID
	}

	@Override
	public Collection<PacketWrapper> toNative() {
		return Collections.singletonList(new PacketWrapper(new Chat(message), Unpooled.wrappedBuffer(readbytes)));
	}

}
