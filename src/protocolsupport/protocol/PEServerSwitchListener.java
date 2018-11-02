package protocolsupport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.RecyclableArrayList;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerPostConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe.ChangeDimensionPacket;
import protocolsupport.protocol.storage.LinkedTokenBasedThrottler;
import protocolsupport.protocol.storage.NetworkDataCache;

import java.util.concurrent.TimeUnit;

public class PEServerSwitchListener implements Listener {

    @EventHandler
    public void handle(ServerPostConnectedEvent event) {
        ConnectionImpl connection = (ConnectionImpl) ProtocolSupportAPI.getConnection(event.getPlayer());
        if (connection == null || connection.getVersion().getProtocolType() != ProtocolType.PE) {// ?Already disconnected
            return;
        }
        NetworkDataCache cache = NetworkDataCache.getFrom(connection);
        if (cache == null || !cache.dimQueue()) {
            return;
        }
        event.registerIntent(ProtocolSupport.get());
        invoke(event, cache, (UserConnection) event.getPlayer());
    }

    @EventHandler
    public void handle(ServerSwitchEvent event) {
        Connection connection = ProtocolSupportAPI.getConnection(event.getPlayer());
        ProtocolVersion version = connection.getVersion();
        if (version.getProtocolType() != ProtocolType.PE) {
            return;
        }
        if (connection.getMetadata("_PE_TRANSFER_") == null) {// FIRST TIME CONNECT SERVER
            connection.addMetadata("_PE_TRANSFER_", "");
            return;
        }
        NetworkDataCache cache = NetworkDataCache.getFrom(connection);
        LinkedTokenBasedThrottler<Long> throttler = cache.getEntityKillThrottler();
        if (!throttler.isEmpty()) {
            ProxyServer.getInstance().getScheduler().schedule(ProtocolSupport.get(), () -> throttledEntityKill(connection, cache), 1, TimeUnit.SECONDS);
        }
    }

    private static void throttledEntityKill(Connection connection, NetworkDataCache cache) {
        if (cache.isAwaitDimensionAck()) {
            ProxyServer.getInstance().getScheduler().schedule(ProtocolSupport.get(), () -> throttledEntityKill(connection, cache), 1, TimeUnit.SECONDS);
            return;
        }
        LinkedTokenBasedThrottler<Long> throttler = cache.getEntityKillThrottler();
        throttler.updateToken(System.currentTimeMillis());
        while (!throttler.isTokenOrObjEmpty()) {
            long id = throttler.useTokenAndObj();
            if (!cache.getWatchedEntities().contains(id)) {
                ((UserConnection) connection.getPlayer()).sendPacket(new PacketWrapper(null, ChangeDimensionPacket.createEntityRemove(connection.getVersion(), id)));
            }
        }
        if (!throttler.isEmpty()) ProxyServer.getInstance().getScheduler().schedule(ProtocolSupport.get(), () -> throttledEntityKill(connection, cache), 1, TimeUnit.SECONDS);
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
            event.getConnector().eventLoop().schedule(() -> invoke(event, cache, user), 1, TimeUnit.SECONDS);
        } else {
            event.completeIntent(ProtocolSupport.get());
        }
    }

}
