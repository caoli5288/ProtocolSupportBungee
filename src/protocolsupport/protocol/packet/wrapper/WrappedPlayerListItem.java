package protocolsupport.protocol.packet.wrapper;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class WrappedPlayerListItem extends PlayerListItem implements IWrappedPacket {

    private ByteBuf origin;

    @Override
    public ByteBuf origin() {
        return origin;
    }

    public void setOrigin(ByteBuf origin) {
        this.origin = origin;
    }
}
