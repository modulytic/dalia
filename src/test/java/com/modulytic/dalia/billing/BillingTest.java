package com.modulytic.dalia.billing;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.app.database.include.DatabaseConstants;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingTest {
    static final int countryCode  = 1;
    static final String messageId = "id";
    static final String smppUser  = "smppuser";

    static final ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

    static Database database;

    @BeforeEach
    void setup() {
        database = mock(Database.class);
        Context.setDatabase(database);
    }

    @Test
    void logMessageWithFoundVroute() {
        Vroute vroute = mock(Vroute.class);
        when(vroute.getCountryCode()).thenReturn(countryCode);
        when(vroute.getId()).thenReturn(1);
        when(vroute.getRate()).thenReturn(0.34f);
        when(vroute.getName()).thenReturn("USA");

        Billing.logMessage(messageId, smppUser, countryCode, vroute);

        verify(database, times(1)).insert(anyString(), captor.capture());

        // Make sure vroute is actually set to be inserted into the database
        Map<String, Object> insertData = captor.getValue();
        assertEquals(vroute.getId(), insertData.get(DatabaseConstants.VROUTE));
        assertEquals(vroute.getRate(), insertData.get(DatabaseConstants.RATE));
    }

    @Test
    void logMessageWithNullVroute() {
        Billing.logMessage(messageId, smppUser, countryCode, null);

        verify(database, times(1)).insert(anyString(), captor.capture());

        Map<String, Object> insertData = captor.getValue();
        assertNull(insertData.get(DatabaseConstants.VROUTE));
        assertNull(insertData.get(DatabaseConstants.RATE));
    }

    @Test
    void logMessageIgnoringVroute() {
        Billing.logMessage(messageId, smppUser, countryCode, null);

        verify(database, times(1)).insert(anyString(), captor.capture());

        Map<String, Object> insertData = captor.getValue();
        assertEquals(messageId, insertData.get(DatabaseConstants.MSG_ID));
        assertEquals(smppUser, insertData.get(DatabaseConstants.SMPP_USER));
        assertEquals(countryCode, insertData.get(DatabaseConstants.COUNTRY_CODE));
    }
}