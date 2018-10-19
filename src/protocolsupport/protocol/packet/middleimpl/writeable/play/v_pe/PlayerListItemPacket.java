package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.packet.wrapper.IWrappedPacket;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.PEPacketIdSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Allocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayerListItemPacket extends WriteableMiddlePacket<PlayerListItem> {

    @Override
    public Collection<ByteBuf> toData(PlayerListItem packet) {
        if (packet instanceof IWrappedPacket) {
            return Collections.singleton(((IWrappedPacket) packet).origin());
        }
        List<ByteBuf> output = new ArrayList<>();
        if (packet.getAction() == PlayerListItem.Action.REMOVE_PLAYER) {
            output.add(remove(connection.getVersion(), packet.getItems()));
        }
        return output;
    }

    public static ByteBuf remove(ProtocolVersion version, PlayerListItem.Item[] input) {
        ByteBuf pk = Allocator.allocateBuffer();
        PEPacketIdSerializer.writePacketId(version, pk, 63);
        pk.writeByte(1);// action remove
        VarNumberSerializer.writeVarInt(pk, input.length);
        Arrays.asList(input).forEach(ele -> MiscSerializer.writeUUIDLE(pk, ele.getUuid()));
        return pk;
    }

}
