package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import gnu.trove.iterator.TLongIterator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.RecyclableArrayList;
import net.md_5.bungee.protocol.packet.Respawn;
import protocolsupport.Environment;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.storage.LinkedTokenBasedThrottler;
import protocolsupport.protocol.storage.NetworkDataCache;
import protocolsupport.utils.netty.Allocator;

import java.util.Collection;
import java.util.Collections;

public class ChangeDimensionPacket extends WriteableMiddlePacket<Respawn> {

    @Override
    public Collection<ByteBuf> toData(Respawn packet) {
        if (!cache.isAwaitDimensionAck()) cache.setAwaitDimensionAck(true);

        LinkedTokenBasedThrottler<Long> throttler = cache.getEntityKillThrottler();
        TLongIterator iterator = cache.getWatchedEntities().iterator();
        while (iterator.hasNext()) throttler.add(iterator.next());
        cache.getWatchedEntities().clear();

        RecyclableArrayList queue = RecyclableArrayList.newInstance();
        if (cache.dimQueue()) {
            queue.add(createPlayStatus(3));// dim status
        }

        ProtocolVersion version = connection.getVersion();

        queue.add(createDimChange(version, packet.getDimension()));// dim change
        addFakeChunk(version, queue);// chunk
        queue.add(createPosition(version, cache));// position

        cache.dimUpdate(queue);

        return Collections.emptyList();
    }

    public static ByteBuf createDimChange(ProtocolVersion version, int dim) {
        ByteBuf changedim = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, changedim, 61);
        VarNumberSerializer.writeSVarInt(changedim, Environment.getByDimension(dim).getPEDimension());
        changedim.writeFloatLE(0); //x
        changedim.writeFloatLE(0); //y
        changedim.writeFloatLE(0); //z
        changedim.writeBoolean(true);
        return changedim;
    }

    public static void addFakeChunk(ProtocolVersion version, RecyclableArrayList sendque) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                sendque.add(createEmptyChunk(version, x, z));
            }
        }
    }

    public ByteBuf createPlayStatus(int value) {
        ByteBuf buf = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(connection.getVersion(), buf, 2);
        buf.writeInt(value);
        return buf;
    }

    private static ByteBuf createPosition(ProtocolVersion version, NetworkDataCache cache) {
        ByteBuf serializer = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, serializer, 19);
        VarNumberSerializer.writeVarLong(serializer, Integer.MAX_VALUE);
        MiscSerializer.writeLFloat(serializer, (float) 0);
        MiscSerializer.writeLFloat(serializer, (float) cache.updateFakeY());
        MiscSerializer.writeLFloat(serializer, (float) 0);
        MiscSerializer.writeLFloat(serializer, 0);
        MiscSerializer.writeLFloat(serializer, 0);
        MiscSerializer.writeLFloat(serializer, 0); //head yaw actually
        serializer.writeByte(1);
        serializer.writeBoolean(false); //on ground
        VarNumberSerializer.writeVarLong(serializer, 0);
        return serializer;
    }

    private static ByteBuf createEmptyChunk(ProtocolVersion version, int x, int z) {
        ByteBuf serializer = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, serializer, 58);
        VarNumberSerializer.writeSVarInt(serializer, x);
        VarNumberSerializer.writeSVarInt(serializer, z);
        ByteBuf chunkdata = Unpooled.buffer();
        chunkdata.writeByte(1); //1st section
        chunkdata.writeByte(1); //New subchunk version!
        chunkdata.writeByte((1 << 1) | 1);  //Runtimeflag and palette id.
        chunkdata.writeZero(512);
        VarNumberSerializer.writeSVarInt(chunkdata, 1); //Palette size
        VarNumberSerializer.writeSVarInt(chunkdata, 0); //Air
        chunkdata.writeZero(512); //heightmap.
        chunkdata.writeZero(256); //Biomedata.
        chunkdata.writeByte(0); //borders
        VarNumberSerializer.writeSVarInt(chunkdata, 0); //extra data
        ArraySerializer.writeVarIntLengthByteArray(serializer, chunkdata);
        return serializer;
    }

    public static ByteBuf createEntityRemove(ProtocolVersion version, long id) {
        ByteBuf buf = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, buf, 14);
        VarInt.writeVarLong(buf, id);
        return buf;
    }

}
