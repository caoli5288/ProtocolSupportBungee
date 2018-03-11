package protocolsupport.protocol.packet.middleimpl.writeable.play.v_pe;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.BossBar;
import protocolsupport.protocol.packet.middle.WriteableMiddlePacket;
import protocolsupport.protocol.serializer.VarInt;
import protocolsupport.utils.netty.Allocator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BossEventPacket extends WriteableMiddlePacket<BossBar> {

    @Override
    public Collection<ByteBuf> toData(BossBar origin) {
        OriginAction action = OriginAction.values()[origin.getAction()];
        return action.transfer(origin);
    }

    enum OriginAction {

        CREATE,

        REMOVE {
            List<ByteBuf> transfer(BossBar origin) {
                ByteBuf pk = Allocator.allocateBuffer();
                VarInt.writeUnsignedVarInt(pk, 74);
                pk.writeByte(0);
                pk.writeByte(0);
                VarInt.writeVarLong(pk, origin.getUuid().getMostSignificantBits());
                VarInt.writeUnsignedVarInt(pk, 2);
                return Collections.singletonList(pk);
            }
        },

        UPDATE_HEALTH,
        UPDATE_DISPLAY,
        UPDATE_STYLE,
        UPDATE_FLAG;

        OriginAction() {
        }

        List<ByteBuf> transfer(BossBar origin) {
            return Collections.emptyList();
        }
    }
}
