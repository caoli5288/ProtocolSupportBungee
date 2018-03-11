package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.Respawn;
import protocolsupport.Environment;
import protocolsupport.protocol.packet.middleimpl.writeable.PESingleWriteablePacket;
import protocolsupport.protocol.serializer.VarNumberSerializer;

public class ToClientRespawnPacket extends PESingleWriteablePacket<Respawn> {

    public ToClientRespawnPacket() {
        super(61);
    }

    @Override
    protected void write(ByteBuf data, Respawn packet) {
        VarNumberSerializer.writeSVarInt(data, Environment.getByDimension(packet.getDimension()).getPEDimension());
        data.writeFloatLE(0); //x
        data.writeFloatLE(0); //y
        data.writeFloatLE(0); //z
        data.writeBoolean(true); //respawn
    }

}
