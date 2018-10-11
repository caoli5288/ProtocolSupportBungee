package protocolsupport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.RecyclableArrayList;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.ServerPostConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.injector.BungeeNettyChannelInjector;
import protocolsupport.protocol.storage.NetworkDataCache;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DimensionUpdate implements Listener {

    @EventHandler
    public void handle(ServerPostConnectedEvent event) {
        ConnectionImpl connection = (ConnectionImpl) ProtocolSupportAPI.getConnection(event.getPlayer());
        if (connection == null) {// ?Already disconnected
            return;
        }
        NetworkDataCache cache = NetworkDataCache.getFrom(connection);
        if (cache == null || !cache.dimQueue()) {
            return;
        }
        event.registerIntent(ProtocolSupport.get());
        invoke(event, cache, (UserConnection) event.getPlayer());
    }

    private void invoke(ServerPostConnectedEvent event, NetworkDataCache cache, UserConnection user) {
        RecyclableArrayList queue = cache.dimUpdate();
        boolean ret = false;
        try {
            for (Object obj : queue) {
                user.sendPacket(new PacketWrapper(null, (ByteBuf) obj));
            }
            ret = true;
        } catch (Exception err) {// disconnected?
            System.out.println("!!! Error in dimension update invoke. " + err.getMessage());
        } finally {
            queue.recycle();
        }
        if (ret && cache.dimQueue() && user.isActive()) {
            event.getConnector().eventLoop().schedule(() -> invoke(event, cache, user), 500, TimeUnit.MILLISECONDS);
        } else {
            event.completeIntent(ProtocolSupport.get());
        }
    }

}
