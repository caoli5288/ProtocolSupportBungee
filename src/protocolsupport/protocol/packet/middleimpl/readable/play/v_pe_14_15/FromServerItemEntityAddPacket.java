package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;

import java.util.Collection;
import java.util.Collections;

public class FromServerItemEntityAddPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 15;

    public FromServerItemEntityAddPacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf from) {
        cache.addWatchedEntity(VarInt.readVarLong(from));
        from.skipBytes(from.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singleton(new PacketWrapper(null, Unpooled.wrappedBuffer(readbytes)));
    }
}
