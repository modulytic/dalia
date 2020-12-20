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
    void allValidStatesAreValid() {
        assertTrue(MessageState.isValid(MessageState.EN_ROUTE));
        assertTrue(MessageState.isValid(MessageState.DELIVERED));
        assertTrue(MessageState.isValid(MessageState.UNKNOWN));
        assertTrue(MessageState.isValid(MessageState.UNDELIVERABLE));
        assertTrue(MessageState.isValid(MessageState.ACCEPTED));
        assertTrue(MessageState.isValid(MessageState.DELETED));
        assertTrue(MessageState.isValid(MessageState.EXPIRED));
        assertTrue(MessageState.isValid(MessageState.REJECTED));
    }

    @Test
    void invalidStatesAreCaught() {
        assertFalse(MessageState.isValid(null));
        assertFalse(MessageState.isValid(""));
        assertFalse(MessageState.isValid("acceptd"));
        assertFalse(MessageState.isValid("accepted"));
    }
}