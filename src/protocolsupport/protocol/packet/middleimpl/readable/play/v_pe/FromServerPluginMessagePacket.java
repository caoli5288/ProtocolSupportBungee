package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.PluginMessage;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.StringSerializer;

import java.util.Collection;
import java.util.Collections;

public class FromServerPluginMessagePacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 250;
    private String tag;
    private byte[] data;

    public FromServerPluginMessagePacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf from) {
        tag = StringSerializer.readVarIntUTF8String(from);
        data = ArraySerializer.readVarIntLengthByteArray(from);
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        return Collections.singletonList(new PacketWrapper(new PluginMessage(tag, data, false), Unpooled.wrappedBuffer(readbytes)));
    }
}
