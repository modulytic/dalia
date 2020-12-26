package com.modulytic.dalia.billing;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.modulytic.dalia.DaliaContext;
import com.modulytic.dalia.local.include.DbManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingManagerTest {
    @Test
    void getNullVroute() {
        DbManager database = mock(DbManager.class);
        when(database.fetch(anyString(), any(LinkedHashMap.class))).thenReturn(null);
        DaliaContext.setDatabase(database);

        BillingManager bm = new BillingManager();
        assertNull(bm.getActiveVroute(1));
    }

    @Test
    void getUSAVroute() {
        Table<Integer, String, Object> vrouteResult = TreeBasedTable.create();
        vrouteResult.put(0, "id", 0);
        vrouteResult.put(0, "vroute_name", "USA");
        vrouteResult.put(0, "rate", 0.34f);
        vrouteResult.put(0, "country_code", 1);
        vrouteResult.put(0, "is_active", true);

        DbManager database = mock(DbManager.class);
        when(database.fetch(anyString(), any(LinkedHashMap.class))).thenReturn(vrouteResult);
        DaliaContext.setDatabase(database);

        BillingManager bm = new BillingManager();
        Vroute vroute = bm.getActiveVroute(1);
        assertNotNull(vroute);

        ArgumentCaptor<LinkedHashMap<String, Object>> argument = ArgumentCaptor.forClass(LinkedHashMap.class);
        verify(database).fetch(anyString(), argument.capture());
        assertEquals(1, (Integer) argument.getValue().get("country_code"));
        assertTrue((Boolean) argument.getValue().get("is_active"));

        assertEquals("USA", vroute.getName());
        assertEquals(0.34f, vroute.getRate());
        assertEquals(0, vroute.getId());
    }

    @Test
    void getNullVrouteFromEmptyResults() {
        Table<Integer, String, Object> vrouteResult = TreeBasedTable.create();

        DbManager database = mock(DbManager.class);
        when(database.fetch(anyString(), any(LinkedHashMap.class))).thenReturn(vrouteResult);
        DaliaContext.setDatabase(database);

        BillingManager bm = new BillingManager();
        assertNull(bm.getActiveVroute(1));

        ArgumentCaptor<LinkedHashMap<String, Object>> argument = ArgumentCaptor.forClass(LinkedHashMap.class);
        verify(database).fetch(anyString(), argument.capture());
        assertEquals(1, (Integer) argument.getValue().get("country_code"));
        assertTrue((Boolean) argument.getValue().get("is_active"));
    }
}