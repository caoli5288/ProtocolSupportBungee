package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import gnu.trove.iterator.TLongIterator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.ServerPostConnectedEvent;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Respawn;
import protocolsupport.Environment;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.storage.LinkedTokenBasedThrottler;
import protocolsupport.protocol.storage.NetworkDataCache;
import protocolsupport.utils.netty.Allocator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChangeDimensionPacket extends WriteableMiddlePacket<Respawn> {

    @Override
    public Collection<ByteBuf> toData(Respawn packet) {
        if (cache.setAwaitDimensionAck(true, false)) {
            cache.setAwaitSpawn(true, false);
            LinkedTokenBasedThrottler<Long> throttler = cache.getEntityKillThrottler();
            TLongIterator iterator = cache.getWatchedEntities().iterator();
            while (iterator.hasNext()) throttler.add(iterator.next());
            cache.getWatchedEntities().clear();
            return create(connection.getVersion(), cache, packet);
        }
        cache.getChangeDimensionQueue().add(packet);
        return Collections.emptyList();
    }

    private static LinkedList<ByteBuf> create(ProtocolVersion version, NetworkDataCache cache, Respawn packet) {
        LinkedList<ByteBuf> packets = new LinkedList<>();
        packets.add(createChangeDimension(version, packet.getDimension()));
        addFakeChunk(version, packets);
        packets.add(createPosition(version, cache));
        return packets;
    }

    public static void onAckReceive(Connection connection, NetworkDataCache cache) {
        if (cache.isAwaitDimensionAck()) {
//            System.out.println("onAckReceive");
            LinkedList<Respawn> queue = cache.getChangeDimensionQueue();
            if (queue.isEmpty()) {
                cache.setAwaitDimensionAck(false, true);
                ServerPostConnectedEvent event = cache.getPostConnector();
                cache.setPostConnector(null);
                event.completeIntent(ProtocolSupport.get());
            } else {
                Respawn packet = queue.remove();
                LinkedList<ByteBuf> packets = create(connection.getVersion(), cache, packet);
                UserConnection conn = (UserConnection) connection.getPlayer();
                conn.sendPacket(new PacketWrapper(null, createPlayStatus(connection.getVersion(), 3)));
                for (ByteBuf buf : packets) {
                    conn.sendPacket(new PacketWrapper(null, buf));
                }
            }
        }
    }

    public static ByteBuf createChangeDimension(ProtocolVersion version, int dim) {
        ByteBuf buf = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, buf, 61);
        VarNumberSerializer.writeSVarInt(buf, Environment.getByDimension(dim).getPEDimension());
        buf.writeFloatLE(0); //x
        buf.writeFloatLE(0); //y
        buf.writeFloatLE(0); //z
        buf.writeBoolean(true);
        return buf;
    }

    public static void addFakeChunk(ProtocolVersion version, List<ByteBuf> sendQue) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                sendQue.add(createEmptyChunk(version, x, z));
            }
        }
    }

    public static ByteBuf createPlayStatus(ProtocolVersion version, int value) {
        ByteBuf buf = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, buf, 2);
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

}
