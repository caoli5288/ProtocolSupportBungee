package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.util.List;
import java.util.zip.Inflater;

public class PEDecompressor extends MessageToMessageDecoder<ByteBuf> {

    private byte[] buf = new byte[1 << 16];// 64k
    private Inflater decompress;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        decompress = new Inflater();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        decompress.end();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> output) throws Exception {
        ByteBuf decompression = ctx.alloc().buffer();
        _input(in);
        try {
            do {
                int len = decompress.inflate(buf);
                decompression.writeBytes(buf, 0, len);
            } while (!decompress.finished());
            _output(decompression, output);
        } finally {
            decompress.reset();
            decompression.release();
        }
    }

    private void _output(ByteBuf buf, List<Object> output) {
        do {
            int len = VarNumberSerializer.readVarInt(buf);
            output.add(buf.retainedSlice(buf.readerIndex(), len));
            buf.skipBytes(len);
        } while (buf.isReadable());
    }

    private void _input(ByteBuf in) {
        if (in.hasArray()) {
            decompress.setInput(in.array(), in.arrayOffset() + in.readerIndex(), in.readableBytes());
            in.skipBytes(in.readableBytes());
            return;
        }
        decompress.setInput(MiscSerializer.readAllBytes(in));
    }

}
