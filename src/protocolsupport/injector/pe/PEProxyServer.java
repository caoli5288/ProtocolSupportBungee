package protocolsupport.injector.pe;

import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import raknetserver.RakNetServer;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class PEProxyServer {

	private final InetSocketAddress listener;

	private RakNetServer peserver;

	public void start() {
		(peserver = new RakNetServer(
				listener,
				PENetServerConstants.PING_HANDLER,
				channel -> {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast(new PECompressor());
					pipeline.addLast(new PEDecompressor());
					pipeline.addLast(new PEProxyNetworkManager());
				}, PENetServerConstants.USER_PACKET_ID
		)).start();
	}

	public void stop() {
		peserver.stop();
	}

}
