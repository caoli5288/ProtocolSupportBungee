package protocolsupport.protocol.utils.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class BiFunctionRegistry<T, U, R> {

    private final Map<Object, BiFunction<T, U, R>> all;

    public BiFunctionRegistry(Map<Object, BiFunction<T, U, R>> all) {
        this.all = all;
    }

    public BiFunctionRegistry() {
        all = new HashMap<>();
    }

    public void register(Object key, BiFunction<T, U, R> function) {
        all.put(key, function);
    }

    public R handle(Object key, T i, U l) {
        if (all.containsKey(key)) {
            return all.get(key).apply(i, l);
        }
        return null;
    }

    public Set<Object> getKeys() {
        return all.keySet();
    }
}
