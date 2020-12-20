package com.modulytic.dalia.smpp.api;

import net.gescobar.smppserver.packet.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SMSCAddressTest {
    @Test
    void invalidPhoneNumber() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("8");

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertFalse(smscAddress.isSupported());
    }

    @Test
    void invalidTon() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(TON.SUBSCRIBER);
        when(address.getNpi()).thenReturn(NPI.E164);

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertFalse(smscAddress.isSupported());
        assertFalse(smscAddress.isValidTon());
        assertTrue(smscAddress.isValidNpi());
    }

    @Test
    void invalidTonAndNpi() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(TON.SUBSCRIBER);
        when(address.getNpi()).thenReturn(NPI.PRIVATE);

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertFalse(smscAddress.isSupported());
        assertFalse(smscAddress.isValidTon());
        assertFalse(smscAddress.isValidNpi());
    }

    @Test
    void valid() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(TON.INTERNATIONAL);
        when(address.getNpi()).thenReturn(NPI.E164);

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertTrue(smscAddress.isSupported());
        assertTrue(smscAddress.isValidTon());
        assertTrue(smscAddress.isValidNpi());
    }

    @Test
    void countryCodeUS() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(TON.INTERNATIONAL);
        when(address.getNpi()).thenReturn(NPI.E164);

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertEquals(1, smscAddress.getCountryCode());
    }

    @Test
    void countryCodeDE() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+4930901820");
        when(address.getTon()).thenReturn(TON.INTERNATIONAL);
        when(address.getNpi()).thenReturn(NPI.E164);

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertEquals(49, smscAddress.getCountryCode());
    }

    @Test
    void e164() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("(555) 869-3564");
        when(address.getTon()).thenReturn(TON.NATIONAL);
        when(address.getNpi()).thenReturn(NPI.NATIONAL);

        SMSCAddress smscAddress = new SMSCAddress(address);
        assertTrue(smscAddress.isSupported());
        assertEquals("+15558693564", smscAddress.toE164());
    }
}