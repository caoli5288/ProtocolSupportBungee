package protocolsupport.protocol.pipeline.version.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import protocolsupport.api.Connection;
import protocolsupport.protocol.storage.NetworkDataCache;

import java.util.List;

public class ToClientEntityRewriteHandler extends MessageToMessageEncoder<ByteBuf> {

	protected final Connection connection;
	protected final NetworkDataCache cache;

	public ToClientEntityRewriteHandler(Connection connection, NetworkDataCache cache) {
		this.connection = connection;
		this.cache = cache;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		out.add(buf.retain());
	}

}
