package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Respawn;
import protocolsupport.Environment;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.util.Collection;
import java.util.Collections;

public class RespawnPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 61;

    public RespawnPacket() {
        super(PACKET_ID);
    }

    protected int dimension;

    @Override
    protected void read0(ByteBuf buf) {
        Environment environment = Environment.getByPEDimension(VarNumberSerializer.readSVarInt(buf));
        dimension = environment.getDimension();
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singletonList(new PacketWrapper(new Respawn(dimension, (short) 0, (short) 0, ""), Unpooled.wrappedBuffer(readbytes)));
    }

}
