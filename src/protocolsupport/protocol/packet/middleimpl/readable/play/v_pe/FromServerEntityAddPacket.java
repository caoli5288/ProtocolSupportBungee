package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;

import java.util.Collection;
import java.util.Collections;

public class FromServerEntityAddPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 13;

    public FromServerEntityAddPacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf buf) {
        cache.addWatchedEntity(VarInt.readVarLong(buf));
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singletonList(new PacketWrapper(null, Unpooled.wrappedBuffer(readbytes)));
    }
}
