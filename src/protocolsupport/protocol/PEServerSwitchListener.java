package protocolsupport.protocol;

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
import protocolsupport.protocol.packet.middleimpl.readable.play.v_pe_14_15.FromServerEntityRemovePacket;
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
        if (cache == null || !cache.isAwaitDimensionAck()) {
            return;
        }
//        System.out.println("cache.isAwaitDimensionAck");
        event.registerIntent(ProtocolSupport.get());
        cache.setPostConnector(event);
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
        }
    }

}
