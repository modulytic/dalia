package com.modulytic.dalia.smpp.api;

import com.modulytic.dalia.smpp.internal.AppAddress;
import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.Npi;
import net.gescobar.smppserver.packet.Ton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppAddressTest {
    @Test
    void invalidPhoneNumber() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("8");

        AppAddress appAddress = new AppAddress(address);
        assertFalse(appAddress.getSupported());
    }

    @Test
    void invalidTon() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(Ton.SUBSCRIBER);
        when(address.getNpi()).thenReturn(Npi.E164);

        AppAddress appAddress = new AppAddress(address);
        assertFalse(appAddress.getSupported());
        assertFalse(appAddress.isValidTon());
        assertTrue(appAddress.isValidNpi());
    }

    @Test
    void invalidTonAndNpi() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(Ton.SUBSCRIBER);
        when(address.getNpi()).thenReturn(Npi.PRIVATE);

        AppAddress appAddress = new AppAddress(address);
        assertFalse(appAddress.getSupported());
        assertFalse(appAddress.isValidTon());
        assertFalse(appAddress.isValidNpi());
    }

    @Test
    void valid() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(Ton.INTERNATIONAL);
        when(address.getNpi()).thenReturn(Npi.E164);

        AppAddress appAddress = new AppAddress(address);
        assertTrue(appAddress.getSupported());
        assertTrue(appAddress.isValidTon());
        assertTrue(appAddress.isValidNpi());
    }

    @Test
    void countryCodeUS() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+15558693564");
        when(address.getTon()).thenReturn(Ton.INTERNATIONAL);
        when(address.getNpi()).thenReturn(Npi.E164);

        AppAddress appAddress = new AppAddress(address);
        assertEquals(1, appAddress.getCountryCode());
    }

    @Test
    void countryCodeDE() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("+4930901820");
        when(address.getTon()).thenReturn(Ton.INTERNATIONAL);
        when(address.getNpi()).thenReturn(Npi.E164);

        AppAddress appAddress = new AppAddress(address);
        assertEquals(49, appAddress.getCountryCode());
    }

    @Test
    void e164() {
        Address address = mock(Address.class);
        when(address.getAddress()).thenReturn("(555) 869-3564");
        when(address.getTon()).thenReturn(Ton.NATIONAL);
        when(address.getNpi()).thenReturn(Npi.NATIONAL);

        AppAddress appAddress = new AppAddress(address);
        assertTrue(appAddress.getSupported());
        assertEquals("+15558693564", appAddress.toE164());
    }
}