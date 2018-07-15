package protocolsupport.injector.pe;

import io.netty.buffer.ByteBuf;
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

    private static final ThreadLocal<byte[]> LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[2 << 21]);
    private final Inflater inflater = new Inflater();

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        inflater.end();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> output) throws Exception {
        try {
            inflater.setInput(MiscSerializer.readAllBytes(buf));
            byte[] decompressionbuffer = LOCAL_BUF.get();
            int decompressedlength = inflater.inflate(decompressionbuffer);
            if (!inflater.finished()) {
                throw new DecoderException(MessageFormat.format("Badly compressed packet - size is larger than protocol maximum of {0}", decompressionbuffer.length));
            }
            ByteBuf uncompresseddata = Unpooled.wrappedBuffer(decompressionbuffer, 0, decompressedlength);
            while (uncompresseddata.isReadable()) {
                output.add(Unpooled.wrappedBuffer(MiscSerializer.readBytes(uncompresseddata, VarNumberSerializer.readVarInt(uncompresseddata))));
            }
        } finally {
            inflater.reset();
        }
    }

}
