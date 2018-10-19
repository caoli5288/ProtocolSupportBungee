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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PEBlockPalette {

    private static Map<String, BlockElement> table = new HashMap<>();
    @Getter
    private static byte[] paletteData;

    static {
        List<JSONObject> root = (List<JSONObject>) JSONValue.parse(new InputStreamReader(ProtocolSupport.get().getResourceAsStream("block_state_list_1_5.json")));

        List<BlockElement> l = new ArrayList<>(root.size());
        for (JSONObject obj : root) {
            l.add(new BlockElement(obj));
        }

        ByteBuf buf = Unpooled.buffer();
        try {
            VarNumberSerializer.writeVarInt(buf, l.size());
            for (BlockElement element : l) {
                StringSerializer.writeVarIntUTF8String(buf, element.name);
                buf.writeShortLE(element.data);
                table.put(element.toString(), element);
            }
            paletteData = MiscSerializer.readAllBytes(buf);
        } finally {
            buf.release();
        }
    }

    public static BlockElement getBlockElement(String nameWithMetadata) {
        return table.get(nameWithMetadata);
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
