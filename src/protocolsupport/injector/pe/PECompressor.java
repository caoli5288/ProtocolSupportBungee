package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Compressor;

public class PECompressor extends MessageToByteEncoder<ByteBuf> {

    private final int threshold;

    public PECompressor(int threshold) {
        super(false);
        this.threshold = Math.max(128, threshold);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            int len = in.readableBytes();
            VarNumberSerializer.writeVarInt(buf, len);
            buf.writeBytes(in);
//            out.writeBytes(compressor.compress(MiscSerializer.readAllBytes(buf)));
            Compressor.compressUnsafe(len <= threshold ? 0 : 1, buf, out);
        } finally {
            buf.release();
        }
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
        return ctx.alloc().heapBuffer(msg.readableBytes() * 11 / 10 + 100);
    }
}
