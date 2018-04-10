package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.Respawn;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.protocol.storage.NetworkDataCache;
import protocolsupport.utils.netty.Allocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChangeDimensionPacket extends WriteableMiddlePacket<Respawn> {

    @Override
    public Collection<ByteBuf> toData(Respawn packet) {
        List<ByteBuf> sendque = new ArrayList<>();
        removeEntities(sendque, cache);
        return sendque;
    }

    public static void removeEntities(List<ByteBuf> sendque, NetworkDataCache cache) {
        TLongHashSet watchedEntities = cache.getWatchedEntities();
        TLongIterator itr = watchedEntities.iterator();
        while (itr.hasNext()) {
            ByteBuf buf = Allocator.allocateBuffer();
            VarInt.writeUnsignedVarInt(buf, 14);
            buf.writeByte(0);
            buf.writeByte(0);
            VarInt.writeVarLong(buf, itr.next());
            sendque.add(buf);
        }
        watchedEntities.clear();
    }

}
