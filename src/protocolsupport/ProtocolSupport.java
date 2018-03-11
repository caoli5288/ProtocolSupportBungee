package protocolsupport;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.yaml.snakeyaml.Yaml;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.injector.BungeeNettyChannelInjector;
import protocolsupport.injector.pe.PEProxyServer;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class ProtocolSupport extends Plugin implements Listener {

	@Getter
	private static HikariDataSource dataSource;
	private PEProxyServer peserver;

	@SneakyThrows
	public void onLoad() {
		try {
			getProxy().getPluginManager().registerCommand(this, new CommandHandler());
			BungeeNettyChannelInjector.inject();
		} catch (Throwable t) {
			t.printStackTrace();
			ProxyServer.getInstance().stop();
		}
	}

	@SneakyThrows
	public void onEnable() {
		getProxy().getPluginManager().registerListener(this, this);

		File dataFolder = getDataFolder();
		if (!dataFolder.isDirectory() && dataFolder.mkdir()) {
			throw new IllegalStateException("mkdir");
		}

		File plugin = new File(dataFolder, "plugin.yml");
		if (!plugin.isFile()) {
			Files.copy(getResourceAsStream("plugin.yml"), plugin.toPath());
		}

		Map<String, ?> load = new Yaml().load(new FileInputStream(plugin));
		Object convert = load.get("naming_convert");
		if (!(convert == null) && ((boolean) convert) && load.containsKey("naming_convert_lobby")) {
			Map<String, String> database = (Map<String, String>) load.get("database");
			dataSource = new HikariDataSource();
			dataSource.setJdbcUrl(database.get("url"));
			dataSource.setUsername(database.get("user"));
			dataSource.setPassword(database.get("password"));
			dataSource.getConnection().close();// fast fail if not connected
			getProxy().getPluginManager().registerListener(this, new NamingConvertListener(load.get("naming_convert_lobby").toString()));
		}

		String listen = (String) load.get("pocket_listen");

		(peserver = new PEProxyServer(listen == null ? getProxy().getConfig().getListeners().iterator().next().getHost() : toInetAddr(listen))).start();
	}

	private InetSocketAddress toInetAddr(String listen) {
		Iterator<String> itr = Arrays.asList(listen.split(":")).iterator();
		return new InetSocketAddress(itr.next(), itr.hasNext() ? Integer.valueOf(itr.next()) : 19132);
	}

	@EventHandler
	public void handle(ServerSwitchEvent event) {
		Connection conn = ProtocolSupportAPI.getConnection(event.getPlayer());
		if (conn.getMetadata("_PE_TRANSFER_") == null) {
			conn.addMetadata("_PE_TRANSFER_", "");
		}
	}

	@Override
	public void onDisable() {
		peserver.stop();
	}

}
