package com.modulytic.dalia.smpp;

import com.modulytic.dalia.local.include.DbManager;
import com.modulytic.dalia.smpp.api.SMSCAddress;
import com.modulytic.dalia.smpp.request.SubmitRequest;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessage;
import net.gescobar.smppserver.Response;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DaliaSmppRequestHandlerTest {
    @Test
    void submitSmUnsupportedNPI() {
        DaliaSmppRequestHandler handler = new DaliaSmppRequestHandler(null);

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.isSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(false);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        Response res = handler.onSubmitSm(req);
        assertEquals(Response.INVALID_DESTINATION_NPI, res);
    }

    @Test
    void submitSmUnsupportedTON() {
        DaliaSmppRequestHandler handler = new DaliaSmppRequestHandler(null);

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.isSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(false);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        Response res = handler.onSubmitSm(req);
        assertEquals(Response.INVALID_DESTINATION_TON, res);
    }

    @Test
    void submitSmUnsupportedDestAddress() {
        DaliaSmppRequestHandler handler = new DaliaSmppRequestHandler(null);

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.isSupported()).thenReturn(false);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        Response res = handler.onSubmitSm(req);
        assertEquals(Response.INVALID_DEST_ADDRESS, res);
    }

    @Test
    void submitSmCallsDatabaseAndWebsocket() {
        DbManager database = mock(DbManager.class);
        WsdServer server = mock(WsdServer.class);
        DaliaSmppRequestHandler handler = new DaliaSmppRequestHandler(database);
        handler.setWsdServer(server);

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.isSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.toEndpointRequest()).thenReturn(new WsdMessage(null, null));

        handler.onSubmitSm(req);
        verify(database, times(1)).fetch(anyString(), any(LinkedHashMap.class));
        verify(server, times(1)).sendNext(any(WsdMessage.class));
    }

    @Test
    void submitSmReturnsSendError() {
        WsdServer server = mock(WsdServer.class);
        when(server.sendNext(any(WsdMessage.class))).thenReturn(false);

        DaliaSmppRequestHandler handler = new DaliaSmppRequestHandler(null);
        handler.setWsdServer(server);

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.isSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        Response res = handler.onSubmitSm(req);
        assertEquals(Response.SYSTEM_ERROR, res);
    }

    @Test
    void submitSmCanPersistDlrToDatabase() {
        DbManager database = mock(DbManager.class);
        WsdServer server = mock(WsdServer.class);
        when(server.sendNext(any(WsdMessage.class))).thenReturn(true);

        DaliaSmppRequestHandler handler = new DaliaSmppRequestHandler(database);
        handler.setWsdServer(server);

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.isSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.toEndpointRequest()).thenReturn(new WsdMessage(null, null));
        when(req.getShouldForwardDLRs()).thenReturn(true);

        handler.onSubmitSm(req);
        verify(database, times(1)).fetch(anyString(), any(LinkedHashMap.class));
        verify(server, times(1)).sendNext(any(WsdMessage.class));
        verify(req, times(1)).persistDLRParamsTo(any(DbManager.class));
    }
}