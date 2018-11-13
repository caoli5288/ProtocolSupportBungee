package protocolsupport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.Getter;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import protocolsupport.ProtocolSupport;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

import java.io.InputStreamReader;
import java.util.List;

public class PEBlockPalette {

    @Getter
    private static byte[] paletteData;

    static {
        List<JSONObject> root = (List<JSONObject>) JSONValue.parse(new InputStreamReader(ProtocolSupport.get().getResourceAsStream("block_state_list_1_5.json")));
        ByteBuf buf = Unpooled.buffer();
        try {
            VarNumberSerializer.writeVarInt(buf, root.size());
            for (JSONObject obj : root) {
                BlockElement element = new BlockElement(obj);
                StringSerializer.writeVarIntUTF8String(buf, element.name);
                buf.writeShortLE(element.data);
            }
            paletteData = MiscSerializer.readAllBytes(buf);
        } finally {
            buf.release();
        }
    }

    @Data
    public static class BlockElement {

        private String name;
        private int data;
        private int runtimeId;

        BlockElement(JSONObject obj) {
            name = String.valueOf(obj.get("name"));
            data = ((Number) obj.get("data")).intValue();
            runtimeId = ((Number) obj.get("runtimeId")).intValue();
        }

        @Override
        public String toString() {
            return name + data;
        }
    }

}
