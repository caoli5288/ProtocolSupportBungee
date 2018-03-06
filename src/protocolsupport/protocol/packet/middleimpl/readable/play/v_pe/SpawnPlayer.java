package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarInt;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class SpawnPlayer extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 12;

    public SpawnPlayer() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf buf) {
        MiscSerializer.readUUID(buf);
        StringSerializer.readVarIntUTF8String(buf);

        long id = VarInt.readVarLong(buf);
        Set<Long> all = (Set<Long>) connection.getMetadata("_HANDLED_ENTITY_");
        if (all == null) {
            connection.addMetadata("_HANDLED_ENTITY_", all = Sets.newConcurrentHashSet());
        }

        all.add(id);
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singletonList(new PacketWrapper(null, Unpooled.wrappedBuffer(readbytes)));
    }

}
