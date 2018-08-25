package protocolsupport.utils.netty;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import protocolsupport.protocol.serializer.MiscSerializer;

public class Decompressor {

	private static final Recycler<Decompressor> recycler = new Recycler<Decompressor>() {
		@Override
		protected Decompressor newObject(Handle<Decompressor> handle) {
			return new Decompressor(handle);
		}
	};

	public static Decompressor create() {
		return recycler.get();
	}

	private final Inflater decompress = new Inflater();
	private final Handle<Decompressor> handle;
	protected Decompressor(Handle<Decompressor> handle) {
		this.handle = handle;
	}

	public byte[] decompress(byte[] input, int uncompressedlength) throws DataFormatException {
		byte[] uncompressed = new byte[uncompressedlength];
		decompress.setInput(input);
		try {
			decompress.inflate(uncompressed);
		} finally {
			decompress.reset();
		}
		return uncompressed;
	}

	public ByteBuf decompress(ByteBuf in, int uncompressed) {
		ByteBuf out = ByteBufAllocator.DEFAULT.heapBuffer(uncompressed);
		_input(in);
		try {
			decompress.inflate(out.array(), out.arrayOffset(), uncompressed);
			out.writerIndex(uncompressed);
		} catch (DataFormatException e) {
			out.release();
			throw new DecoderException(e);
		} finally {
			decompress.reset();
		}
		return out;
	}

	private void _input(ByteBuf in) {
		if (in.hasArray()) {
			decompress.setInput(in.array(), in.arrayOffset() + in.readerIndex(), in.readableBytes());
			in.skipBytes(in.readableBytes());
			return;
		}
		decompress.setInput(MiscSerializer.readAllBytes(in));
	}

	public void recycle() {
		handle.recycle(this);
	}

	public static byte[] decompressStatic(byte[] input, int uncompressedlength) throws DataFormatException {
		Decompressor decompressor = create();
		try {
			return decompressor.decompress(input, uncompressedlength);
		} finally {
			decompressor.recycle();
		}
	}

}
