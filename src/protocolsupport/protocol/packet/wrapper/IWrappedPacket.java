package protocolsupport.protocol.packet.wrapper;

import io.netty.buffer.ByteBuf;

public interface IWrappedPacket {

    ByteBuf origin();
}
