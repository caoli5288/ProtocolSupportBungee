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
import java.util.LinkedList;
import java.util.List;

public class PEBlockPalette {

    @Getter
    private static byte[] paletteData;
    private static final List<BlockElement> EXTERNAL_ELEMENT_LIST = new LinkedList<>();

    static {
        register("minecraft:mod_ore", 16);// NET_EASE ADD-ON
        List<JSONObject> root = (List<JSONObject>) JSONValue.parse(new InputStreamReader(ProtocolSupport.get().getResourceAsStream("block_state_list_1_5.json")));
        ByteBuf buf = Unpooled.buffer();
        try {
            VarNumberSerializer.writeVarInt(buf, root.size() + EXTERNAL_ELEMENT_LIST.size());
            for (JSONObject obj : root) new BlockElement(obj).write(buf);
            for (BlockElement element : EXTERNAL_ELEMENT_LIST) element.write(buf);
            paletteData = MiscSerializer.readAllBytes(buf);
        } finally {
            buf.release();
        }
    }

    private static void register(String mcKey, int count) {
        for (int i = 0; i < count; i++) {
            EXTERNAL_ELEMENT_LIST.add(new BlockElement(mcKey, i));
        }
    }

    @Data
    public static class BlockElement {

        private String name;
        private int data;

        BlockElement(JSONObject obj) {
            name = String.valueOf(obj.get("name"));
            data = ((Number) obj.get("data")).intValue();
        }

        BlockElement(String name, int data) {
            this.name = name;
            this.data = data;
        }

        @Override
        public String toString() {
            return name + data;
        }

        void write(ByteBuf buf) {
            StringSerializer.writeVarIntUTF8String(buf, name);
            buf.writeShortLE(data);
        }

    }

}
