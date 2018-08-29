package protocolsupport.protocol.pipeline.common;

import java.text.MessageFormat;
import java.util.List;
import java.util.zip.DataFormatException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Decompressor;

public class PacketDecompressor extends ByteToMessageDecoder {

	private static final int MAX_PACKET_LENGTH = 2 << 21;// 16m

	private final Decompressor decompressor = Decompressor.create();

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		decompressor.recycle();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf from, List<Object> list) throws DataFormatException {
		int uncompressed = VarNumberSerializer.readVarInt(from);
		if (uncompressed == 0) {
			list.add(from.retainedSlice());
			from.skipBytes(from.readableBytes());
		} else {
			if (uncompressed > MAX_PACKET_LENGTH) {
				throw new DecoderException(MessageFormat.format("Badly compressed packet - size of {0} is larger than protocol maximum of {1}", uncompressed, MAX_PACKET_LENGTH));
			}
			list.add(decompressor.decompress(from, uncompressed));
		}
	}

}
