package com.modulytic.dalia.smpp;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.smpp.event.DaliaSmppRequestHandler;
import com.modulytic.dalia.smpp.internal.SMSCAddress;
import com.modulytic.dalia.smpp.request.SubmitRequest;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessage;
import com.modulytic.dalia.ws.api.WsdMessageCode;
import com.modulytic.dalia.ws.include.WsdStatusListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DaliaSmppRequestHandlerTest {
    DaliaSmppRequestHandler handler;
    Database database;
    WsdServer server;

    @BeforeEach
    void setup() {
        handler = new DaliaSmppRequestHandler();
        handler.setSmppUser("smppuser");
        database = mock(Database.class);
        server = mock(WsdServer.class);

        Context.setDatabase(database);
        Context.setWsdServer(server);
    }

    @Test
    void submitSmUnsupportedNPI() {
        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(false);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        verify(res).send(argument.capture());
        assertEquals(Response.INVALID_DESTINATION_NPI, argument.getValue());
    }

    @Test
    void submitSmUnsupportedTON() {
        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(false);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        verify(res).send(argument.capture());
        assertEquals(Response.INVALID_DESTINATION_TON, argument.getValue());
    }

    @Test
    void submitSmUnsupportedDestAddress() {
        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(false);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        verify(res).send(argument.capture());
        assertEquals(Response.INVALID_DEST_ADDRESS, argument.getValue());
    }

    @Test
    void submitSmCallsDatabaseAndWebsocket() {
        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.toEndpointRequest()).thenReturn(new WsdMessage(null, null));
        when(req.getMessageId()).thenReturn("df90ef49-a382-4c60-a5ac-806cc1be5063");

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        verify(database, times(1)).fetch(anyString(), any(Map.class));
        verify(server, times(1)).sendNext(any(WsdMessage.class), any(WsdStatusListener.class));
    }

    @Test
    void submitSmReturnsNoClientsError() {
        ResponseSender res = mock(ResponseSender.class);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            WsdStatusListener listener = (WsdStatusListener) args[1];
            listener.onStatus(WsdMessageCode.NO_CLIENTS);

            ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
            verify(res).send(argument.capture());
            assertEquals(Response.SYSTEM_ERROR, argument.getValue());

            return null;
        }).when(server).sendNext(any(WsdMessage.class), any(WsdStatusListener.class));

        handler.setSmppUser("smppuser");

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.getMessageId()).thenReturn("df90ef49-a382-4c60-a5ac-806cc1be5063");

        handler.onSubmitSm(req, res);
    }

    @Test
    void submitSmReturnsSendError() {
        ResponseSender res = mock(ResponseSender.class);

        WsdServer server = mock(WsdServer.class);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            WsdStatusListener listener = (WsdStatusListener) args[1];
            listener.onStatus(3);       // random non-success code

            ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
            verify(res).send(argument.capture());
            assertEquals(Response.SYSTEM_ERROR, argument.getValue());

            return null;
        }).when(server).sendNext(any(WsdMessage.class), any(WsdStatusListener.class));

        handler.setSmppUser("smppuser");

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.getMessageId()).thenReturn("df90ef49-a382-4c60-a5ac-806cc1be5063");

        handler.onSubmitSm(req, res);
    }

    @Test
    void submitSmReturnsSuccessOnWsDaemonSuccess() {
        ResponseSender res = mock(ResponseSender.class);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            WsdStatusListener listener = (WsdStatusListener) args[1];
            listener.onStatus(WsdMessageCode.SUCCESS);       // random non-success code

            ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
            verify(res).send(argument.capture());
            assertEquals(Response.OK, argument.getValue());

            return null;
        }).when(server).sendNext(any(WsdMessage.class), any(WsdStatusListener.class));

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.getMessageId()).thenReturn("df90ef49-a382-4c60-a5ac-806cc1be5063");

        handler.onSubmitSm(req, res);
    }

    @Test
    void submitSmCanPersistDlrToDatabase() {
        // tell Dalia message was sent successfully
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            WsdStatusListener listener = (WsdStatusListener) args[1];
            listener.onStatus(WsdMessageCode.SUCCESS);
            return null;
        }).when(server).sendNext(any(WsdMessage.class), any(WsdStatusListener.class));

        SMSCAddress address = mock(SMSCAddress.class);
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        SubmitRequest req = mock(SubmitRequest.class);
        when(req.getDestAddress()).thenReturn(address);
        when(req.toEndpointRequest()).thenReturn(new WsdMessage(null, null));
        when(req.getShouldForwardDLRs()).thenReturn(true);

        when(req.getMessageId()).thenReturn("df90ef49-a382-4c60-a5ac-806cc1be5063");

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);
        verify(database, times(1)).fetch(anyString(), any(Map.class));
        verify(server, times(1)).sendNext(any(WsdMessage.class), any(WsdStatusListener.class));
        verify(req, times(1)).persistDLRParamsTo(any(Database.class));
    }
}