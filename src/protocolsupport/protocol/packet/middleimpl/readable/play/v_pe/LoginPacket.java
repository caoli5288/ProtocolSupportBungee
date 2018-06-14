package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

public class LoginPacket extends PEDefinedReadableMiddlePacket {

	public static final int PACKET_ID = 11;

	public LoginPacket() {
		super(PACKET_ID);
	}

	protected int entityId;
	protected byte gamemode;
	protected int dimension;
	protected int difficulty;

	@Override
	protected void read0(ByteBuf from) {
		VarNumberSerializer.readVarLong(from); //entity id (but it's actually signed varlong, so we use the field below, which is unsigned)
		entityId = (int) VarNumberSerializer.readVarLong(from);
		gamemode = (byte) VarNumberSerializer.readSVarInt(from);
		from.readFloatLE(); //x
		from.readFloatLE(); //y
		from.readFloatLE(); //z
		from.readFloatLE(); //yaw
		from.readFloatLE(); //pitch
		// LEVEL SETTING
		VarNumberSerializer.readSVarInt(from); //seed
		dimension = getPcDimension(VarNumberSerializer.readSVarInt(from));
		VarNumberSerializer.readSVarInt(from); //world type (1 - infinite)
		VarNumberSerializer.readSVarInt(from); // world gamemode (SURVIVAL)
		difficulty = VarNumberSerializer.readSVarInt(from);
		from.skipBytes(from.readableBytes());
	}

	@Override
	public Collection<PacketWrapper> toNative() {
		return Arrays.asList(
			new PacketWrapper(new LoginSuccess(), Unpooled.EMPTY_BUFFER),
			new PacketWrapper(new Login(entityId, gamemode, dimension, (short) difficulty, (short) 1, "", false), Unpooled.wrappedBuffer(readbytes))
		);
	}

	private static int getPcDimension(int dimId) {
		switch (dimId) {
			case 1: {
				return -1;
			}
			case 2: {
				return 1;
			}
			case 0: {
				return 0;
			}
			default: {
				throw new IllegalArgumentException(MessageFormat.format("Uknown dim id {0}", dimId));
			}
		}
	}

}
