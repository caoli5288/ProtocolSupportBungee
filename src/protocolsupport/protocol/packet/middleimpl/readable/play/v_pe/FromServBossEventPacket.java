package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.BossBar;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class FromServBossEventPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 74;
    private long unique;
    private int action;

    public FromServBossEventPacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf buf) {
        unique = VarInt.readVarLong(buf);
        action = VarNumberSerializer.readVarInt(buf);

        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        BossBar bar;
        switch (action) {
            case 0: {
                bar = new BossBar(new UUID(unique, -1), 0);
                break;
            }
            case 2: {
                bar = new BossBar(new UUID(unique, -1), 1);
                break;
            }
            default: {
                bar = null;
                break;
            }
        }
        return Collections.singletonList(new PacketWrapper(bar, Unpooled.wrappedBuffer(readbytes)));
    }
}
