package com.modulytic.dalia.smpp.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisteredDeliveryTest {
    @Test
    void doNotForwardDlrs() {
        RegisteredDelivery rd1 = new RegisteredDelivery((byte)0b0000);
        assertFalse(rd1.getForwardDlrs());
        assertFalse(rd1.getReceiveFinal());
        assertFalse(rd1.getFailureOnly());
        assertFalse(rd1.getIntermediate());
    }

    @Test
    void wantsFinalDlrs() {
        RegisteredDelivery rd1 = new RegisteredDelivery((byte)0b0001);
        assertTrue(rd1.getForwardDlrs());
        assertTrue(rd1.getReceiveFinal());
        assertFalse(rd1.getFailureOnly());
        assertFalse(rd1.getIntermediate());
    }

    @Test
    void wantsErrorDlrs() {
        RegisteredDelivery rd1 = new RegisteredDelivery((byte)0b0010);
        assertTrue(rd1.getForwardDlrs());
        assertFalse(rd1.getReceiveFinal());
        assertTrue(rd1.getFailureOnly());
        assertFalse(rd1.getIntermediate());
    }

    @Test
    void wantsErrorAndIntermediateDlrs() {
        RegisteredDelivery rd1 = new RegisteredDelivery((byte)0b10010);
        assertTrue(rd1.getForwardDlrs());
        assertFalse(rd1.getReceiveFinal());
        assertTrue(rd1.getFailureOnly());
        assertTrue(rd1.getIntermediate());
    }

    @Test
    void wantsOnlyIntermediateDlrs() {
        RegisteredDelivery rd1 = new RegisteredDelivery((byte)0b10000);
        assertTrue(rd1.getForwardDlrs());
        assertFalse(rd1.getReceiveFinal());
        assertFalse(rd1.getFailureOnly());
        assertTrue(rd1.getIntermediate());
    }

    @Test
    void wantsAllDlrs() {
        RegisteredDelivery rd1 = new RegisteredDelivery((byte)0b10001);
        assertTrue(rd1.getForwardDlrs());
        assertTrue(rd1.getReceiveFinal());
        assertFalse(rd1.getFailureOnly());
        assertTrue(rd1.getIntermediate());

        RegisteredDelivery rd2 = new RegisteredDelivery((byte)0b10011);
        assertTrue(rd2.getForwardDlrs());
        assertTrue(rd2.getReceiveFinal());
        assertFalse(rd2.getFailureOnly());
        assertTrue(rd2.getIntermediate());
    }
}