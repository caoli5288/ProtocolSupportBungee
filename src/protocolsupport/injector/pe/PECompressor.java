package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Allocator;
import protocolsupport.utils.netty.Compressor;

public class PECompressor extends MessageToByteEncoder<ByteBuf> {

    private final int threshold;
    private final Compressor fast = Compressor.create(3);

    public PECompressor(int threshold) {
        this.threshold = Math.max(128, threshold);
    }

    private final Compressor noop = Compressor.create(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            int len = in.readableBytes();
            VarNumberSerializer.writeVarInt(buf, len);
            buf.writeBytes(in);
            Compressor compressor = len < threshold ? noop : fast;
            out.writeBytes(compressor.compress(MiscSerializer.readAllBytes(buf)));
        } finally {
            buf.release();
        }
    }

}
