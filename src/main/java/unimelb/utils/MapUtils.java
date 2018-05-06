package unimelb.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtils {

    public static <K, V> Map<K, V> createMap(K[] keys, V[] values) {
        assert keys != null && values != null && keys.length > 0 && values.length > 0 && keys.length == values.length;
        Map<K, V> map = new LinkedHashMap<K, V>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}
