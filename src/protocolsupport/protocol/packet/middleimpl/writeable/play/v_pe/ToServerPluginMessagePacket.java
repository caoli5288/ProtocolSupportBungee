package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.protocol.packet.PluginMessage;
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe.FromServerPluginMessagePacket;
import protocolsupport.protocol.packet.middleimpl.writeable.PESingleWriteablePacket;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.StringSerializer;

public class ToServerPluginMessagePacket extends PESingleWriteablePacket<PluginMessage> {

    public ToServerPluginMessagePacket() {
        super(FromServerPluginMessagePacket.PACKET_ID);
    }

    @Override
    protected void write(ByteBuf data, PluginMessage packet) {
        StringSerializer.writeVarIntUTF8String(data, packet.getTag());
        ArraySerializer.writeVarIntLengthByteArray(data, Unpooled.wrappedBuffer(packet.getData()));
    }
}
