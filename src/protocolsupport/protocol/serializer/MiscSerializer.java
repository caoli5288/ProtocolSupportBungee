package protocolsupport.protocol.serializer;

import java.text.MessageFormat;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

public class MiscSerializer {

	public static UUID readUUID(ByteBuf from) {
		return new UUID(from.readLong(), from.readLong());
	}

	public static UUID readUUIDLE(ByteBuf from) {
		return new UUID(from.readLongLE(), from.readLongLE());
	}

	public static void writeUUID(ByteBuf to, UUID uuid) {
		to.writeLong(uuid.getMostSignificantBits());
		to.writeLong(uuid.getLeastSignificantBits());
	}

	public static void writeUUIDLE(ByteBuf to, UUID uuid) {
		to.writeLongLE(uuid.getMostSignificantBits());
		to.writeLongLE(uuid.getLeastSignificantBits());
	}

	public static void writeLFloat(ByteBuf to, float f) {
		to.writeIntLE(Float.floatToIntBits(f));
	}

	public static float readLFloat(ByteBuf from) {
		return Float.intBitsToFloat(from.readIntLE());
	}

	public static byte[] readAllBytes(ByteBuf buf) {
		return MiscSerializer.readBytes(buf, buf.readableBytes());
	}

	public static byte[] readBytes(ByteBuf buf, int length) {
		byte[] result = new byte[length];
		buf.readBytes(result);
		return result;
	}

	protected static void checkLimit(int length, int limit) {
		if (length > limit) {
			throw new DecoderException(MessageFormat.format("Size {0} is bigger than allowed {1}", length, limit));
		}
	}

	public static void nullVarArray(ByteBuf buf) {
		buf.skipBytes(VarNumberSerializer.readVarInt(buf));
	}

}
