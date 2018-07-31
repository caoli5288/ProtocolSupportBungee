package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Allocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class PlayerListItemPacket extends WriteableMiddlePacket<PlayerListItem> {

    @Override
    public Collection<ByteBuf> toData(PlayerListItem origin) {
        switch (origin.getAction()) {
            case ADD_PLAYER:
            case UPDATE_GAMEMODE:
            case UPDATE_LATENCY:
            case UPDATE_DISPLAY_NAME:
                ArrayList<ByteBuf> output = Lists.newArrayList(cache.getLastTabList());
                cache.getLastTabList().clear();
                return output;
            case REMOVE_PLAYER:
                return createRemove(origin.getItems());// Handle origin bungeecord behavior
            default:
                throw new IllegalStateException();
        }
    }

    public static Collection<ByteBuf> createRemove(PlayerListItem.Item[] input) {
        ByteBuf pk = Allocator.allocateBuffer();
        VarNumberSerializer.writeVarInt(pk, 63);
        pk.writeByte(0);
        pk.writeByte(0);
        pk.writeByte(1);// action remove
        VarNumberSerializer.writeVarInt(pk, input.length);
        Arrays.asList(input).forEach(ele -> MiscSerializer.writeUUIDLE(pk, ele.getUuid()));
        return Collections.singletonList(pk);
    }

}
