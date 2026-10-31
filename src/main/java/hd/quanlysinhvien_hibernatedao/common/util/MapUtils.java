package hd.quanlysinhvien_hibernatedao.common.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MapUtils {

    private MapUtils() {
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValues(Map<K, V> map) {
        Objects.requireNonNull(map, "map must not be null");

        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    public static <K, V> Map<K, V> toMap(
            Iterable<V> values,
            Function<? super V, ? extends K> keyExtractor
    ) {
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");

        Map<K, V> result = new LinkedHashMap<>();

        for (V value : values) {
            Objects.requireNonNull(value, "value must not be null");
            K key = Objects.requireNonNull(keyExtractor.apply(value), "key must not be null");

            if (result.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key: " + key);
            }

            result.put(key, value);
        }

        return Collections.unmodifiableMap(result);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        Objects.requireNonNull(map, "map must not be null");

        Map<V, K> result = new LinkedHashMap<>();
        map.forEach((key, value) -> result.put(value, key));
        return result;
    }

    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }
}
