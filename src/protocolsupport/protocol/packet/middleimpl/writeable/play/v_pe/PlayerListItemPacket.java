package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Allocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

public class PlayerListItemPacket extends WriteableMiddlePacket<PlayerListItem> {

    @Override
    public Collection<ByteBuf> toData(PlayerListItem packet) {
        List<ByteBuf> output = new ArrayList<>();
        Queue<ByteBuf> lastTabList = cache.getLastTabList();
        if (!lastTabList.isEmpty()) {
            output.addAll(lastTabList);
            lastTabList.clear();
        }
        if (packet.getAction() == PlayerListItem.Action.REMOVE_PLAYER) {
            output.add(remove(packet.getItems()));
        }
        return output;
    }

    public static ByteBuf remove(PlayerListItem.Item[] input) {
        ByteBuf pk = Allocator.allocateBuffer();
        VarNumberSerializer.writeVarInt(pk, 63);
        pk.writeByte(0);
        pk.writeByte(0);
        pk.writeByte(1);// action remove
        VarNumberSerializer.writeVarInt(pk, input.length);
        Arrays.asList(input).forEach(ele -> MiscSerializer.writeUUIDLE(pk, ele.getUuid()));
        return pk;
    }

}
