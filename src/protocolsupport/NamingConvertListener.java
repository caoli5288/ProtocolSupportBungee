package protocolsupport;

import com.google.common.collect.Sets;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.packet.handler.PSInitialHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.UUID;

public class NamingConvertListener implements Listener {

    private final Set<UUID> handled = Sets.newConcurrentHashSet();
    private final String lobby;

    public NamingConvertListener(String lobby) {
        this.lobby = lobby;
    }

    @EventHandler
    @SneakyThrows
    public void handle(LoginEvent event) {
        PSInitialHandler connection = (PSInitialHandler) event.getConnection();
        ConnectionImpl l = RefHelper.getField(connection, "connection");
        try {
            String lookup = lookupLocal(connection.getUniqueId(), connection.getName(), l.getVersion() == ProtocolVersion.MINECRAFT_PE);
            if (lookup == null) {
                handled.add(connection.getUniqueId());
            } else {
                connection.getLoginProfile().setName(lookup);
                RefHelper.setField(connection, "username", lookup);
            }
        } catch (Exception ignored) {
            handled.add(connection.getUniqueId());
            @Cleanup Connection conn = ProtocolSupport.getDataSource().getConnection();
            @Cleanup PreparedStatement sql = conn.prepareStatement("insert into localprofile(id, name_origin, pc_pe) value(? ,?, ?)");
            sql.setString(1, connection.getUniqueId().toString());
            sql.setString(2, connection.getName());
            sql.setString(3, l.getVersion() == ProtocolVersion.MINECRAFT_PE ? "pe" : "pc");
            sql.executeUpdate();
        }
    }

    @SneakyThrows
    public String lookupLocal(UUID id, String fallback, boolean pocket) {
        @Cleanup Connection conn = ProtocolSupport.getDataSource().getConnection();
        @Cleanup PreparedStatement sql = conn.prepareStatement("select name from localprofile where id = ?");
        sql.setString(1, id.toString());
        @Cleanup ResultSet set = sql.executeQuery();
        if (set.next()) {
            String name = set.getString("name");
            return name;
        }
        @Cleanup PreparedStatement sqll = conn.prepareStatement("insert into localprofile(id, name, name_origin, pc_pe) value(? ,?, ?, ?)");
        sqll.setString(1, id.toString());
        sqll.setString(2, fallback);
        sqll.setString(3, fallback);
        sqll.setString(4, pocket ? "pe" : "pc");
        sqll.executeUpdate();
        return fallback;
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void handle(ServerConnectEvent event) {
        if (handled.contains(event.getPlayer().getUniqueId())) {
            event.setTarget(BungeeCord.getInstance().getServerInfo(lobby));
        }
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        handled.remove(event.getPlayer().getUniqueId());
    }

}
