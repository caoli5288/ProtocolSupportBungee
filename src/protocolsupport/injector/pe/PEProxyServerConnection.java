package protocolsupport.injector.pe;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.Varint21LengthFieldPrepender;
import protocolsupport.protocol.pipeline.common.EncapsulatedConnectionKeepAlive;
import protocolsupport.protocol.pipeline.common.EncapsulatedHandshakeSender;
import protocolsupport.protocol.pipeline.common.PacketCompressor;
import protocolsupport.protocol.pipeline.common.PacketDecompressor;
import protocolsupport.protocol.pipeline.common.VarIntFrameDecoder;
import protocolsupport.utils.netty.ChannelInitializer;

import java.net.InetSocketAddress;

public class PEProxyServerConnection extends ChannelInboundHandlerAdapter {

	protected final Channel clientconnection;
	protected final ByteBuf handshakepacket;
	public PEProxyServerConnection(Channel clientchannel, ByteBuf handshakepacket) {
		this.clientconnection = clientchannel;
		this.handshakepacket = handshakepacket;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(handshakepacket).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		clientconnection.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.channel().close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		clientconnection.close();
		super.channelInactive(ctx);
	}

	protected static final NioEventLoopGroup group = new NioEventLoopGroup();

	public static Channel connectToServer(Channel peclientchannel, ByteBuf handshakepacket) {
		InetSocketAddress remote = (InetSocketAddress) peclientchannel.remoteAddress();
		handshakepacket.readerIndex(0);
		return new Bootstrap()
		.channel(NioSocketChannel.class)
		.group(group)
		.handler(new ChannelInitializer() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline()
//				.addLast("ps-encap-hs-sender", new EncapsulatedHandshakeSender(remote, true))
				.addLast("flush-consolidation", new FlushConsolidationHandler())
				.addLast("ps-encap-hs-sender", new EncapsulatedHandshakeSender(remote, false))
				.addLast("keepalive", new EncapsulatedConnectionKeepAlive())
				.addLast("prepender", new Varint21LengthFieldPrepender())
				.addLast("splitter", new VarIntFrameDecoder())
//				.addLast("compress", new PacketCompressor(1, BungeeCord.getInstance().config.getCompressionThreshold()))
//				.addLast("decompress", new PacketDecompressor())
				.addLast("handler", new PEProxyServerConnection(peclientchannel, handshakepacket));
			}
		})
		.connect(BungeeCord.getInstance().getConfig().getListeners().iterator().next().getHost())
		.channel();
	}

}
