package protocolsupport.protocol.pipeline.version.v_pe_15;

import io.netty.channel.Channel;
import protocolsupport.api.Connection;
import protocolsupport.protocol.pipeline.IPipeLineBuilder;

public class PipeLineBuilder extends IPipeLineBuilder {
    @Override
    public void buildBungeeClientCodec(Channel channel, Connection connection) {

    }

    @Override
    public void buildBungeeClientPipeLine(Channel channel, Connection connection) {
        throw new UnsupportedOperationException("Only connection through encapsulation protocol is supported");
    }

    @Override
    public void buildBungeeServer(Channel channel, Connection connection) {

    }
}
