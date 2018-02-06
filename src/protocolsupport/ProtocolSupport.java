package protocolsupport;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.injector.BungeeNettyChannelInjector;
import protocolsupport.injector.pe.PEProxyServer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Allocator;

import java.util.Set;

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
		} else {
			Set<Long> all = (Set<Long>) conn.getMetadata("_HANDLED_BAR_");
			if (all == null || all.isEmpty()) {
				return ;
			}
			conn.removeMetadata("_HANDLED_BAR_");
			all.forEach(unique -> {
				ByteBuf buf = Allocator.allocateBuffer();
				VarNumberSerializer.writeVarInt(buf, 14);
				buf.writeByte(0);
				buf.writeByte(0);
				VarNumberSerializer.writeSVarLong(buf, unique);
				((UserConnection) event.getPlayer()).sendPacket(new PacketWrapper(null, buf));

				buf = Allocator.allocateBuffer();
				VarNumberSerializer.writeVarInt(buf, 74);
				buf.writeByte(0);
				buf.writeByte(0);
				VarNumberSerializer.writeSVarLong(buf, unique);
				VarNumberSerializer.writeVarInt(buf, 2);
				((UserConnection) event.getPlayer()).sendPacket(new PacketWrapper(null, buf));
			});

		}
	}

	@Override
	public void onDisable() {
		peserver.stop();
	}

}
