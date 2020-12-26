package com.modulytic.dalia.smpp.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageStateTest {
    @Test
    void allFinalStatesAreFinal() {
        assertTrue(MessageState.isFinal(MessageState.DELIVERED));
        assertTrue(MessageState.isFinal(MessageState.UNDELIVERABLE));
        assertTrue(MessageState.isFinal(MessageState.REJECTED));
        assertTrue(MessageState.isFinal(MessageState.EXPIRED));
        assertTrue(MessageState.isFinal(MessageState.DELETED));
    }

    @Test
    void allIntermediateStatesAreNotFinal() {
        assertFalse(MessageState.isFinal(MessageState.ACCEPTED));
        assertFalse(MessageState.isFinal(MessageState.EN_ROUTE));
        assertFalse(MessageState.isFinal(MessageState.UNKNOWN));
    }

    @Test
    void allErrorStatesAreErrors() {
        assertTrue(MessageState.isError(MessageState.UNDELIVERABLE));
        assertTrue(MessageState.isError(MessageState.REJECTED));
        assertTrue(MessageState.isError(MessageState.EXPIRED));
    }

    @Test
    void allOkayStatesAreNotErrors() {
        assertFalse(MessageState.isError(MessageState.DELIVERED));
        assertFalse(MessageState.isError(MessageState.EN_ROUTE));
        assertFalse(MessageState.isError(MessageState.ACCEPTED));
        assertFalse(MessageState.isError(MessageState.UNKNOWN));
        assertFalse(MessageState.isError(MessageState.DELETED));
    }

    @Test
    void invalidStatesAreCaught() {
        assertNull(MessageState.fromCode(null));
        assertNull(MessageState.fromCode(""));
        assertNull(MessageState.fromCode("acceptd"));
        assertNull(MessageState.fromCode("accepted"));
    }

    @Test
    void statesAreConvertedToStrings() {
        assertEquals(MessageState.ACCEPTED.toString(),      "ACCEPTD");
        assertEquals(MessageState.DELETED.toString(),       "DELETED");
        assertEquals(MessageState.DELIVERED.toString(),     "DELIVRD");
        assertEquals(MessageState.EN_ROUTE.toString(),      "ENROUTE");
        assertEquals(MessageState.EXPIRED.toString(),       "EXPIRED");
        assertEquals(MessageState.REJECTED.toString(),      "REJECTD");
        assertEquals(MessageState.UNDELIVERABLE.toString(), "UNDELIV");
        assertEquals(MessageState.UNKNOWN.toString(),       "UNKNOWN");
    }

    @Test
    void statesAreCreatedFromStrings() {
        assertEquals(MessageState.ACCEPTED,      MessageState.fromCode("ACCEPTD"));
        assertEquals(MessageState.DELETED,       MessageState.fromCode("DELETED"));
        assertEquals(MessageState.DELIVERED,     MessageState.fromCode("DELIVRD"));
        assertEquals(MessageState.EN_ROUTE,      MessageState.fromCode("ENROUTE"));
        assertEquals(MessageState.EXPIRED,       MessageState.fromCode("EXPIRED"));
        assertEquals(MessageState.REJECTED,      MessageState.fromCode("REJECTD"));
        assertEquals(MessageState.UNDELIVERABLE, MessageState.fromCode("UNDELIV"));
        assertEquals(MessageState.UNKNOWN,       MessageState.fromCode("UNKNOWN"));
    }
}