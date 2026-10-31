package hd.quanlysinhvien_hibernatedao.common.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapUtilsTest {

    private record Item(Long id, String name) {
    }

    @Test
    void sortMapByValues_sortsDescending() {
        Map<String, Integer> source = new LinkedHashMap<>();
        source.put("A", 2);
        source.put("B", 5);
        source.put("C", 3);

        Map<String, Integer> result = MapUtils.sortMapByValues(source);

        assertEquals(List.of("B", "C", "A"), List.copyOf(result.keySet()));
        assertEquals(List.of("A", "B", "C"), List.copyOf(source.keySet()));
    }

    @Test
    void sortMapByValues_emptyMap() {
        assertTrue(MapUtils.sortMapByValues(Map.<String, Integer>of()).isEmpty());
    }

    @Test
    void toMap_indexesUniqueValues() {
        Item first = new Item(1L, "First");
        Item second = new Item(2L, "Second");

        Map<Long, Item> result = MapUtils.toMap(List.of(first, second), Item::id);

        assertEquals(first, result.get(1L));
        assertEquals(second, result.get(2L));
        assertEquals(List.of(1L, 2L), List.copyOf(result.keySet()));
    }

    @Test
    void toMap_rejectsDuplicateKeys() {
        assertThrows(IllegalArgumentException.class,
                () -> MapUtils.toMap(
                        List.of(new Item(1L, "First"), new Item(1L, "Second")),
                        Item::id
                ));
    }

    @Test
    void toMap_returnsUnmodifiableMap() {
        Map<Long, Item> result = MapUtils.toMap(
                List.of(new Item(1L, "First")), Item::id);

        assertThrows(UnsupportedOperationException.class,
                () -> result.put(2L, new Item(2L, "Second")));
    }

    @Test
    void isEmpty_supportsNullAndEmptyMap() {
        assertTrue(MapUtils.isEmpty(null));
        assertTrue(MapUtils.isEmpty(Map.of()));
        assertFalse(MapUtils.isEmpty(Map.of("A", 1)));
    }

    @Test
    void isNotEmpty_supportsNullAndNonEmptyMap() {
        assertFalse(MapUtils.isNotEmpty(null));
        assertFalse(MapUtils.isNotEmpty(Map.of()));
        assertTrue(MapUtils.isNotEmpty(Map.of("A", 1)));
    }

    @Test
    void invertMap_exchangesKeysAndValues() {
        Map<String, Integer> source = new LinkedHashMap<>();
        source.put("A", 1);
        source.put("B", 2);

        assertEquals(Map.of(1, "A", 2, "B"), MapUtils.invertMap(source));
    }

    @Test
    void invertMap_repeatedValuesUseLastKey() {
        Map<String, Integer> source = new LinkedHashMap<>();
        source.put("A", 1);
        source.put("B", 1);

        assertEquals(Map.of(1, "B"), MapUtils.invertMap(source));
    }

    @Test
    void emptyMap_isEmptyAndUnmodifiable() {
        Map<String, Integer> result = MapUtils.emptyMap();

        assertTrue(result.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> result.put("A", 1));
    }
}
