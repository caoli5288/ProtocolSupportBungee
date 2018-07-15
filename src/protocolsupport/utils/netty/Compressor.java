package protocolsupport.utils.netty;

import java.util.Arrays;
import java.util.zip.Deflater;

public class Compressor{

    public static Compressor create(int level) {
        return new Compressor(level);
    }

    private final Deflater deflater;

    protected Compressor(int level) {
        deflater = new Deflater(level);
    }

    public byte[] compress(byte[] input) {
        deflater.setInput(input);
        deflater.finish();
        byte[] compressedbuf = new byte[((input.length * 11) / 10) + 50];
        int size = deflater.deflate(compressedbuf);
        deflater.reset();
        return Arrays.copyOf(compressedbuf, size);
    }

}
