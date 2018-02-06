package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class BossEventPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 74;

    public BossEventPacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf buf) {
        long unique = VarInt.readVarLong(buf);
        int action = VarNumberSerializer.readVarInt(buf);
        switch (action) {
            case 0: {
                Set<Long> all = (Set<Long>) connection.getMetadata("_HANDLED_BAR_");
                if (all == null) {
                    connection.addMetadata("_HANDLED_BAR_", all = Sets.newConcurrentHashSet());
                }
                all.add(unique);
            }
            break;
            case 2: {
                Set<Long> all = (Set<Long>) connection.getMetadata("_HANDLED_BAR_");
                if (!(all == null)) all.remove(unique);
            }
            break;
        }
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singletonList(new PacketWrapper(null, Unpooled.wrappedBuffer(readbytes)));
    }
}
