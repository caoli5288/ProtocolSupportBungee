package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import java.util.Arrays;
import java.util.Collection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import protocolsupport.protocol.packet.id.PEPacketId;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

public class LoginPacket extends PEDefinedReadableMiddlePacket {

	public LoginPacket() {
		super(PEPacketId.Clientbound.PLAY_START_GAME);
	}

	protected int entityId;
	protected byte gamemode;
	protected int dimension;
	protected int difficulty;

	@Override
	protected void read0(ByteBuf from) {
		VarNumberSerializer.readSVarLong(from); //entity id (but it's actually signed varlong, so we use the field below, which is unsigned)
		entityId = (int) VarNumberSerializer.readVarLong(from);
		gamemode = (byte) VarNumberSerializer.readSVarInt(from);
		from.readFloatLE(); //x
		from.readFloatLE(); //y
		from.readFloatLE(); //z
		from.readFloatLE(); //yaw
		from.readFloatLE(); //pitch
		VarNumberSerializer.readSVarInt(from); //seed
		dimension = RespawnPacket.getPcDimensionId(VarNumberSerializer.readSVarInt(from));
		VarNumberSerializer.readSVarInt(from); //world type (1 - infinite)
		VarNumberSerializer.readSVarInt(from); // world gamemode (SURVIVAL)
		difficulty = VarNumberSerializer.readSVarInt(from);
		VarNumberSerializer.readSVarInt(from); //world spawn x
		VarNumberSerializer.readVarInt(from); //world spawn y
		VarNumberSerializer.readSVarInt(from); //world spawn z
		from.readBoolean(); //disable achievements
		VarNumberSerializer.readSVarInt(from); //time
		from.readBoolean(); //edu mode
		from.readFloatLE(); //rain level
		from.readFloatLE(); //lighting level
		from.readBoolean(); //is multiplayer
		from.readBoolean(); //broadcast to lan
		from.readBoolean(); //broadcast to xbl
		from.readBoolean(); //commands enabled
		from.readBoolean(); //needs texture pack
		VarNumberSerializer.readVarInt(from); //game rules //TODO: actually implement gamerules reading in case pspe will actually send them one day
		from.readBoolean(); //player map enabled
		from.readBoolean(); //trust players
		VarNumberSerializer.readSVarInt(from); //permission level
		VarNumberSerializer.readSVarInt(from); //game publish setting
		from.readIntLE(); //chunk tick radius
		StringSerializer.readVarIntUTF8String(from); //level id (pe one)
		StringSerializer.readVarIntUTF8String(from); //level name (will packet.getLevelType() work?)
		StringSerializer.readVarIntUTF8String(from); //template pack id
		from.readBoolean(); //is trial
		from.readLongLE(); //level time
		VarNumberSerializer.readSVarInt(from); //enchantment seed
	}

	@Override
	public Collection<PacketWrapper> toNative() {
		return Arrays.asList(
			new PacketWrapper(new LoginSuccess(), Unpooled.EMPTY_BUFFER),
			new PacketWrapper(new Login(entityId, gamemode, dimension, (short) difficulty, (short) 1, "", false), Unpooled.wrappedBuffer(readbytes))
		);
	}

}
