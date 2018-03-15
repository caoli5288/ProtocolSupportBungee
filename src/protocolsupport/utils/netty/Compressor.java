package protocolsupport.utils.netty;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;

import java.util.Arrays;
import java.util.zip.Deflater;

public class Compressor {

	private static final Recycler<Compressor> recycler = new Recycler<Compressor>() {
		@Override
		protected Compressor newObject(Handle<Compressor> handle) {
			return new Compressor(handle);
		}
	};

	public static Compressor create() {
		return recycler.get();
	}

	private final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
	private final Handle<Compressor> handle;

	protected Compressor(Handle<Compressor> handle) {
		this.handle = handle;
	}

	public byte[] compress(byte[] input) {
		deflater.setInput(input);
		deflater.finish();
		byte[] compressedBuf = new byte[((input.length * 11) / 10) + 50];
		int size = deflater.deflate(compressedBuf);
		deflater.reset();
		return Arrays.copyOf(compressedBuf, size);
	}

	public void recycle() {
		handle.recycle(this);
	}
}
