package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.utils.netty.Allocator;

import java.util.Collection;
import java.util.Collections;

public class FromServerEntityRemovePacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 14;

    public FromServerEntityRemovePacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf buf) {
        cache.removeWatchedEntity(VarInt.readVarLong(buf));
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singletonList(new PacketWrapper(null, Unpooled.wrappedBuffer(readbytes)));
    }

    public static ByteBuf createEntityRemove(ProtocolVersion version, long id) {
        ByteBuf buf = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, buf, 14);
        VarInt.writeVarLong(buf, id);
        return buf;
    }

}
