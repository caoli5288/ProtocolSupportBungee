package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.text.MessageFormat;
import java.util.List;
import java.util.zip.Inflater;

public class PEDecompressor extends MessageToMessageDecoder<ByteBuf> {

    private Inflater inflater;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        inflater = new Inflater();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        inflater.end();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> output) throws Exception {
        ByteBuf decompression = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            inflater.reset();
            inflater.setInput(MiscSerializer.readAllBytes(in));
            byte[] buf = new byte[inflater.getRemaining() << 1];
            for (int ret; (ret = inflater.inflate(buf)) >= 1;) {
                decompression.writeBytes(buf, 0, ret);
            }
            while (decompression.isReadable()) {
                int length = VarNumberSerializer.readVarInt(decompression);
                output.add(Unpooled.wrappedBuffer(MiscSerializer.readBytes(decompression, length)));
            }
        } finally {
            decompression.release();
        }
    }

}
