package protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.ServerPostConnectedEvent;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middleimpl.readable.PEDefinedReadableMiddlePacket;
import protocolsupport.protocol.packet.wrapper.WrappedPlayerListItem;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.utils.registry.BiFunctionRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerListItemPacket extends PEDefinedReadableMiddlePacket {

    public static final int PACKET_ID = 63;
    private static final BiFunctionRegistry<ProtocolVersion, ByteBuf, UUID> ENTRY_PROCESS_REGISTRY = new BiFunctionRegistry<>();

    static {
        ENTRY_PROCESS_REGISTRY.register(PlayerListItem.Action.ADD_PLAYER, (version, buf) -> {
            UUID id = MiscSerializer.readUUIDLE(buf);
            VarNumberSerializer.readVarInt(buf);// Entity id
            MiscSerializer.nullVarArray(buf);// name
            if (version.isBefore(ProtocolVersion.MINECRAFT_PE_1_7)) {
                MiscSerializer.nullVarArray(buf);// 3rd party
                VarInt.readVarInt(buf);
            }
            MiscSerializer.nullVarArray(buf);
            MiscSerializer.nullVarArray(buf);
            MiscSerializer.nullVarArray(buf);
            MiscSerializer.nullVarArray(buf);
            MiscSerializer.nullVarArray(buf);
            MiscSerializer.nullVarArray(buf);// x-uid
            MiscSerializer.nullVarArray(buf);// platform chat id
            return id;
        });
        ENTRY_PROCESS_REGISTRY.register(PlayerListItem.Action.REMOVE_PLAYER, (version, buf) -> MiscSerializer.readUUIDLE(buf));
    }

    private PlayerListItem.Action action;
    private List<UUID> elements;

    public PlayerListItemPacket() {
        super(PACKET_ID);
    }

    @Override
    protected void read0(ByteBuf packet) {
        byte actionId = packet.readByte();
        switch (actionId) {
            case 0:
                action = PlayerListItem.Action.ADD_PLAYER;
                break;
            case 1:
                action = PlayerListItem.Action.REMOVE_PLAYER;
                break;
        }

        int len = Math.toIntExact(VarNumberSerializer.readVarInt(packet));
        elements = new ArrayList<>();
        for (int i = len; i >= 1; i--) {
            elements.add(ENTRY_PROCESS_REGISTRY.handle(action, connection.getVersion(), packet));
        }

        packet.skipBytes(packet.readableBytes());
    }

    @Override
    public Collection<PacketWrapper> toNative() {
        WrappedPlayerListItem item = new WrappedPlayerListItem();
        item.setOrigin(Unpooled.wrappedBuffer(readbytes));
        item.setAction(action);
        item.setItems(elements.stream().map(el -> {
            PlayerListItem.Item i = new PlayerListItem.Item();
            i.setUuid(el);
            return i;
        }).toArray(PlayerListItem.Item[]::new));
        ServerPostConnectedEvent connector = cache.getPostConnector();
        if (connector != null) {
            /*
             * We handle player list packets manually when post connector active.
             */
            ((UserConnection) connection.getPlayer()).getTabListHandler().onUpdate(item);
            return Collections.emptyList();
        }
        return Collections.singletonList(new PacketWrapper(item, Unpooled.wrappedBuffer(readbytes)));
    }

}
