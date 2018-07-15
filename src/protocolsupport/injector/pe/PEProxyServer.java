package protocolsupport.injector.pe;

import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import raknetserver.RakNetServer;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class PEProxyServer {

	private final InetSocketAddress listener;

	private RakNetServer peserver;

	public void start() {
		(peserver = new RakNetServer(
				listener,
				new PEProxyServerInfoHandler(),
				channel -> {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast(new PECompressor(BungeeCord.getInstance().config.getCompressionThreshold()));
					pipeline.addLast(new PEDecompressor());
					pipeline.addLast(PEProxyNetworkManager.NAME, new PEProxyNetworkManager());
				}, 0xFE
		)).start();
	}

	public void stop() {
		peserver.stop();
	}

}
