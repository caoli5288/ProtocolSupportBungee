package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.utils.netty.Allocator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class PlayerListItemPacket extends WriteableMiddlePacket<PlayerListItem> {

    @Override
    public Collection<ByteBuf> toData(PlayerListItem origin) {
        switch (origin.getAction()) {
            case REMOVE_PLAYER:
                return createRemove(origin.getItems());
            default:
                return Collections.emptyList();
        }
    }

    public static Collection<ByteBuf> createRemove(PlayerListItem.Item[] input) {
        ByteBuf pk = Allocator.allocateBuffer();
        VarInt.writeUnsignedVarInt(pk, 63);
        pk.writeByte(0);
        pk.writeByte(0);
        pk.writeByte(1);// action remove
        VarInt.writeUnsignedVarInt(pk, input.length);
        Arrays.asList(input).forEach(ele -> {
            VarInt.writeUnsignedVarLong(pk, ele.getUuid().getMostSignificantBits());
            VarInt.writeUnsignedVarLong(pk, ele.getUuid().getLeastSignificantBits());
        });
        return Collections.singletonList(pk);
    }

}
