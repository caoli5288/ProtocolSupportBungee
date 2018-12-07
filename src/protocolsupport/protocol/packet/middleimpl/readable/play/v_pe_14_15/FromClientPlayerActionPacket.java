package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe.ChangeDimensionPacket;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.util.Collection;
import java.util.Collections;

public class FromClientPlayerActionPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 36;

    public static final int UNKNOWN = -1,
            START_BREAK = 0,
            ABORT_BREAK = 1,
            STOP_BREAK = 2,
            GET_UPDATED_BLOCK = 3,
            RELEASE_ITEM = 4,
            START_SLEEPING = 5,
            STOP_SLEEPING = 6,
            SPAWNED = 7,
            JUMP = 8,
            START_SPRINT = 9,
            STOP_SPRINT = 10,
            START_SNEAK = 11,
            STOP_SNEAK = 12,
            DIMENSION_CHANGE = 13,
            DIMENSION_CHANGE_ACK = 14,
            START_GLIDE = 15,
            STOP_GLIDE = 16,
            BUILD_DENIED = 17,
            CONTINUE_BREAK = 18,
            CHANGE_SKIN = 19,
            SET_ENCHANTMENT_SEED = 20,
            START_SWIMMING = 21,
            STOP_SWIMMING = 22,
            START_SPIN_ATTACK = 23,
            STOP_SPIN_ATTACK = 24;

    public FromClientPlayerActionPacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf from) {
        VarNumberSerializer.readVarLong(from);// ENTITY ID
        int action = VarNumberSerializer.readSVarInt(from);
//        System.out.println("action id " + action);
        switch (action) {
            case DIMENSION_CHANGE_ACK: {
                ChangeDimensionPacket.onAckReceive(connection, cache);
                break;
            }
        }
        from.readerIndex(from.readerIndex() + from.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singleton(new PacketWrapper(null, Unpooled.wrappedBuffer(readbytes)));
    }
}
