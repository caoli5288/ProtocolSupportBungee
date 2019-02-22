package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PEProxyNetworkManager extends ChannelInboundHandlerAdapter {

	public static final String NAME = "peproxy-nm";

	protected Channel serverconnection;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		if (serverconnection == null) {
			serverconnection = PEProxyServerConnection.connectToServer(ctx.channel(), buf);
		} else {
			serverconnection.writeAndFlush(buf).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.err.println("PE proxy client connection exception occured");
		cause.printStackTrace();
		ctx.channel().close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (serverconnection != null) {
			serverconnection.close();
		}
		super.channelInactive(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		if (serverconnection != null) {
			serverconnection.close();
		}
		super.channelUnregistered(ctx);
	}

}
