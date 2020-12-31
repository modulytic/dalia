package com.modulytic.dalia.app.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RowStoreTest {
    private RowStore rowStore;

    @BeforeEach
    public void setup() {
        rowStore = new RowStore();
    }

    @Test
    public void get() {
        rowStore.add("test", "value");
        assertEquals("value", rowStore.get("test"));
    }

    @Test
    public void getSuperClass() {
        rowStore.add("test", "value", String.class);
        // should not fail, String is a subclass of Object
        Object o = rowStore.get("test");
    }

    @Test
    public void getWrongType() {
        rowStore.add("test", "value");

        assertThrows(ClassCastException.class, () -> {
            int value = rowStore.get("test");
        });
    }

    @Test
    public void getImplicitType() {
        rowStore.add("test", "value");
        assertEquals(String.class, rowStore.getType("test"));
    }

    @Test
    public void getExplicitType() {
        rowStore.add("test", 0.34f, Float.class);
        assertEquals(Float.class, rowStore.getType("test"));
    }

    @Test
    public void replace() {
        rowStore.add("test", "kiki");
        rowStore.replace("test", 0.3f);
        assertEquals(0.3f, (float) rowStore.get("test"));
        assertEquals(Float.class, rowStore.getType("test"));
    }

    @Test
    public void isEmpty() {
        assertTrue(rowStore.isEmpty());
        rowStore.add("test", "val");
        assertFalse(rowStore.isEmpty());
    }
}