package com.modulytic.dalia.billing;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VrouteTest {
    @Test
    void mapConstructor() {
        Map<String, Object> dbParams = ImmutableMap.of("id", 0, "vroute_name", "USA", "rate", 0.34f, "country_code", 1);
        Vroute v = new Vroute(dbParams);

        assertEquals(0, v.getId());
        assertEquals("USA", v.getName());
        assertEquals(0.34f, v.getRate());
        assertEquals(1, v.getCountryCode());
    }
}