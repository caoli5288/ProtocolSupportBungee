package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.utils.netty.Allocator;
import protocolsupport.utils.netty.Compressor;

public class PECompressor extends MessageToByteEncoder<ByteBuf> {

    private final ByteBuf buf = Allocator.allocateBuffer();
    private final int threshold;
    private final Compressor fast = Compressor.create(3);

    public PECompressor(int threshold) {
        this.threshold = Math.max(128, threshold);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        buf.release();
    }

    private final Compressor noop = Compressor.create(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf out) throws Exception {
        this.buf.clear();
        int len = buf.readableBytes();
        VarNumberSerializer.writeVarInt(this.buf, len);
        this.buf.writeBytes(buf);
        Compressor compressor = len < threshold ? noop : fast;
        out.writeBytes(compressor.compress(MiscSerializer.readAllBytes(this.buf)));
    }

}
