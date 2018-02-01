package protocolsupport;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.injector.BungeeNettyChannelInjector;
import protocolsupport.injector.pe.PEProxyServer;

public class ProtocolSupport extends Plugin implements Listener {

	private PEProxyServer peserver;

	@Override
	public void onLoad() {
		try {
			getProxy().getPluginManager().registerCommand(this, new CommandHandler());
			BungeeNettyChannelInjector.inject();
		} catch (Throwable t) {
			t.printStackTrace();
			ProxyServer.getInstance().stop();
		}
	}

	@Override
	public void onEnable() {
		(peserver = new PEProxyServer()).start();
		getProxy().getPluginManager().registerListener(this, this);
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
