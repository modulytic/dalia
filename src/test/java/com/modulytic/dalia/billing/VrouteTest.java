package com.modulytic.dalia.billing;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.DatabaseResults;
import com.modulytic.dalia.app.database.RowStore;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.app.database.include.DatabaseConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class VrouteTest {
    @Test
    void getNullVroute() {
        Database database = mock(Database.class);
        when(database.fetch(anyString(), any(LinkedHashMap.class))).thenReturn(null);
        Context.setDatabase(database);

        assertNull(Vroute.getActiveVroute(1));
    }

    @Test
    void getUSAVroute() {
        DatabaseResults res = new DatabaseResults();
        RowStore r = new RowStore();
        r.add(DatabaseConstants.VROUTE_ID, 0, Integer.class);
        r.add(DatabaseConstants.VROUTE_NAME, "USA", String.class);
        r.add(DatabaseConstants.RATE, 0.34f, Float.class);
        r.add(DatabaseConstants.COUNTRY_CODE, 1, Integer.class);
        r.add(DatabaseConstants.IS_ACTIVE, true, Boolean.class);
        res.add(r);

        Database database = mock(Database.class);
        when(database.fetch(anyString(), any(Map.class))).thenReturn(res);
        Context.setDatabase(database);

        Vroute vroute = Vroute.getActiveVroute(1);
        assertNotNull(vroute);

        ArgumentCaptor<Map<String, Object>> argument = ArgumentCaptor.forClass(Map.class);
        verify(database).fetch(anyString(), argument.capture());
        assertEquals(1, (Integer) argument.getValue().get(DatabaseConstants.COUNTRY_CODE));
        assertTrue((Boolean) argument.getValue().get(DatabaseConstants.IS_ACTIVE));

        assertEquals("USA", vroute.getName());
        assertEquals(0.34f, vroute.getRate());
        assertEquals(0, vroute.getId());
    }

    @Test
    void getNullVrouteFromEmptyResults() {
        DatabaseResults res = new DatabaseResults();

        Database database = mock(Database.class);
        when(database.fetch(anyString(), any(LinkedHashMap.class))).thenReturn(res);
        Context.setDatabase(database);

        assertNull(Vroute.getActiveVroute(1));

        ArgumentCaptor<Map<String, Object>> argument = ArgumentCaptor.forClass(Map.class);
        verify(database).fetch(anyString(), argument.capture());
        assertEquals(1, (Integer) argument.getValue().get(DatabaseConstants.COUNTRY_CODE));
        assertTrue((Boolean) argument.getValue().get(DatabaseConstants.IS_ACTIVE));
    }

    @Test
    void rowConstructor() {
        RowStore r = new RowStore();
        r.add("id", 0, Integer.class);
        r.add("vroute_name", "USA", String.class);
        r.add("rate", 0.34f, Float.class);
        r.add("country_code", 1, Integer.class);

        Vroute v = new Vroute(r);

        assertEquals(0, v.getId());
        assertEquals("USA", v.getName());
        assertEquals(0.34f, v.getRate());
        assertEquals(1, v.getCountryCode());
    }
}