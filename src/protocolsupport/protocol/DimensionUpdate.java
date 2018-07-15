package protocolsupport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.RecyclableArrayList;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.ServerPostConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.injector.BungeeNettyChannelInjector;
import protocolsupport.protocol.storage.NetworkDataCache;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DimensionUpdate implements Listener {

    @EventHandler
    public void handle(ServerPostConnectedEvent event) {
        ConnectionImpl connection = (ConnectionImpl) ProtocolSupportAPI.getConnection(event.getPlayer());
        NetworkDataCache cache = NetworkDataCache.getFrom(connection);
        if (cache == null || !cache.dimQueue()) {
            return;
        }
        Runnable origin = event.getRunnable();
        event.setRunnable(() -> writeAndWait(connection, cache, cache.dimUpdate(), origin));
    }

    private void writeAndWait(ConnectionImpl connection, NetworkDataCache cache, RecyclableArrayList update, Runnable origin) {
        UserConnection user = (UserConnection) connection.getPlayer();
        for (ByteBuf buf : (List<ByteBuf>) (List) update) {
            user.sendPacket(new PacketWrapper(null, buf));
        }
        update.recycle();

        if (!cache.dimQueue()) {
            origin.run();
            return;
        }

        ((BungeeNettyChannelInjector.CustomHandlerBoss) connection.getNetworkManager()).getChannel().eventLoop().schedule(() -> writeAndWait(connection, cache, cache.dimUpdate(), origin), 500, TimeUnit.MILLISECONDS);
    }
}
