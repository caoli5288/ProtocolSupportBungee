package protocolsupport.injector.pe;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import net.md_5.bungee.BungeeCord;
import protocolsupport.api.ProtocolVersion;
import raknetserver.pipeline.raknet.RakNetPacketConnectionEstablishHandler.PingHandler;

import java.util.concurrent.atomic.AtomicReference;

public class PENetServerConstants {

	private static final AtomicReference<String> REFERENCE = new AtomicReference<>("i5mc");

	public static final PingHandler PING_HANDLER = new PingHandler() {
		@Override
		public String getServerInfo(Channel channel) {
			BungeeCord server = BungeeCord.getInstance();
			int online = server.getOnlineCount();
			return String.join(";",
					"MCPE",
					REFERENCE.get(),
					String.valueOf(ProtocolVersion.MINECRAFT_PE.getId()), POCKET_VERSION,
					online + "", server.config.getPlayerLimit() + ""
			);
		}
		@Override
		public void executeHandler(Runnable runnable) {
			runnable.run();
		}
	};

	public static void setWelcomeMessage(String welcome) {
		Preconditions.checkNotNull(welcome);
		REFERENCE.set(welcome);
	}

	public static final int USER_PACKET_ID = 0xFE;
	public static final String POCKET_VERSION = "1.2.7";

}
