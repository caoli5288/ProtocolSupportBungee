package protocolsupport.utils.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import io.netty.util.Recycler;
import lombok.experimental.Delegate;
import protocolsupport.protocol.serializer.MiscSerializer;

import java.util.Arrays;
import java.util.zip.Deflater;

public class Compressor {

    private final static Handler[] HANDLER_BY_LEVEL = new Handler[Deflater.BEST_COMPRESSION + 1];

    static {
        for (int level = Deflater.NO_COMPRESSION; level < HANDLER_BY_LEVEL.length; level++) {
            HANDLER_BY_LEVEL[level] = new Handler(level);
        }
    }

    private final int level;

    public Compressor(int level) {
        this.level = level;
    }

    public void compress(ByteBuf in, ByteBuf out) {
        if (in.hasArray() && out.hasArray()) {
            if (out.writableBytes() < (in.readableBytes() * 11 / 10 + 50)) {
                throw new EncoderException("compress");
            }
            compressUnsafe(level, in, out);
        } else {
            byte[] inArray = MiscSerializer.readAllBytes(in);
            out.writeBytes(compress(inArray));
        }
    }

    public static void compressUnsafe(int level, ByteBuf in, ByteBuf out) {
        Handled handled = HANDLER_BY_LEVEL[level].get();
        try {
            handled.setInput(in.array(), in.arrayOffset() + in.readerIndex(), in.readableBytes());
            handled.finish();
            int numBytes;
            do {
                int writerIndex = out.writerIndex();
                numBytes = handled.deflate(out.array(), out.arrayOffset() + writerIndex, out.writableBytes(), Deflater.SYNC_FLUSH);
                out.writerIndex(writerIndex + numBytes);
            } while (!handled.needsInput() || numBytes > 0);
            in.readerIndex(in.readerIndex() + in.readableBytes());// new read index at last
        } finally {
            handled.recycle();
        }
    }

    public byte[] compress(byte[] input) {
        Handled handled = HANDLER_BY_LEVEL[level].get();
        try {
            handled.setInput(input);
            handled.finish();
            byte[] compressed = new byte[((input.length * 11) / 10) + 50];
            int len = handled.deflate(compressed);
            return Arrays.copyOf(compressed, len);
        } finally {
            handled.recycle();
        }
    }

    private static class Handled {

        private final Recycler.Handle<Handled> handle;
        @Delegate
        private final Deflater deflater;

        private Handled(Recycler.Handle<Handled> handle, Deflater deflater) {
            this.handle = handle;
            this.deflater = deflater;
        }

        private void recycle() {
            deflater.reset();
            handle.recycle(this);
        }
    }

    private static class Handler extends Recycler<Handled> {

        private final int level;

        private Handler(int level) {
            this.level = level;
        }

        @Override
        protected Handled newObject(Handle<Handled> handle) {
            return new Handled(handle, new Deflater(level));
        }

    }

}
