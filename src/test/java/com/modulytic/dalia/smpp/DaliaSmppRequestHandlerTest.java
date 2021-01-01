package com.modulytic.dalia.smpp;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.event.DaliaSmppRequestHandler;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.smpp.internal.PduBridge;
import com.modulytic.dalia.ws.WsdMessageConverter;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessage;
import com.modulytic.dalia.ws.api.WsdResponseCode;
import com.modulytic.dalia.ws.include.WsdStatusListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.SubmitSm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DaliaSmppRequestHandlerTest {
    DaliaSmppRequestHandler handler;
    Database database;
    static MockedStatic<WsdServer> wsd = mockStatic(WsdServer.class);
    static MockedStatic<WsdMessageConverter> wmc = mockStatic(WsdMessageConverter.class);
    AppAddress address;
    RegisteredDelivery registeredDelivery;
    PduBridge<SubmitSm> req;

    @BeforeEach
    void setup() {
        database = mock(Database.class);
        Context.setDatabase(database);

        handler = new DaliaSmppRequestHandler();
        handler.setSmppUser("smppuser");

        address = mock(AppAddress.class);

        registeredDelivery = mock(RegisteredDelivery.class);

        req = mock(PduBridge.class);
        when(req.getPdu()).thenReturn(mock(SubmitSm.class));
        when(req.getSourceAddress()).thenReturn(address);
        when(req.getDestAddress()).thenReturn(address);
        when(req.getRegisteredDelivery()).thenReturn(registeredDelivery);

        wsd.reset();
        wmc.reset();
        wmc.when(() -> WsdMessageConverter.toMessage(any(PduBridge.class), anyString()))
                .thenReturn(new WsdMessage(null, null));
    }

    @AfterAll
    static void end() {
        wsd.close();
    }

    @Test
    void submitSmUnsupportedNPI() {
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(false);
        when(address.isValidTon()).thenReturn(true);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        verify(res).send(argument.capture());
        assertEquals(Response.INVALID_DESTINATION_NPI, argument.getValue());
    }

    @Test
    void submitSmUnsupportedTON() {
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(false);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        verify(res).send(argument.capture());
        assertEquals(Response.INVALID_DESTINATION_TON, argument.getValue());
    }

    @Test
    void submitSmUnsupportedDestAddress() {
        when(address.getSupported()).thenReturn(false);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        verify(res).send(argument.capture());
        assertEquals(Response.INVALID_DEST_ADDRESS, argument.getValue());
    }

    @Test
    void submitSmCallsDatabaseAndWebsocket() {
        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);

        verify(database, times(1)).fetch(anyString(), any(Map.class));
        wsd.verify(() -> WsdServer.sendNext(any(WsdMessage.class), any(WsdStatusListener.class)));
    }

    @Test
    void submitSmReturnsNoClientsError() {
        ResponseSender res = mock(ResponseSender.class);

        wsd.when(() -> WsdServer.sendNext(any(WsdMessage.class), any(WsdStatusListener.class)))
            .then(invocation -> {
                Object[] args = invocation.getArguments();
                WsdStatusListener listener = (WsdStatusListener) args[1];
                listener.onStatus(WsdResponseCode.NO_CLIENTS);

                ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
                verify(res).send(argument.capture());
                assertEquals(Response.SYSTEM_ERROR, argument.getValue());

                return null;
            });

        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        handler.onSubmitSm(req, res);
    }

    @Test
    void submitSmReturnsSendError() {
        ResponseSender res = mock(ResponseSender.class);

        wsd.when(() -> WsdServer.sendNext(any(WsdMessage.class), any(WsdStatusListener.class)))
            .then(invocation -> {
                Object[] args = invocation.getArguments();
                WsdStatusListener listener = (WsdStatusListener) args[1];
                listener.onStatus(3);       // random non-success code

                ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
                verify(res).send(argument.capture());
                assertEquals(Response.SYSTEM_ERROR, argument.getValue());

                return null;
            });

        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        handler.onSubmitSm(req, res);
    }

    @Test
    void submitSmReturnsSuccessOnWsDaemonSuccess() {
        ResponseSender res = mock(ResponseSender.class);

        wsd.when(() -> WsdServer.sendNext(any(WsdMessage.class), any(WsdStatusListener.class)))
            .then(invocation -> {
                Object[] args = invocation.getArguments();
                WsdStatusListener listener = (WsdStatusListener) args[1];
                listener.onStatus(WsdResponseCode.SUCCESS);       // random non-success code

                ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
                verify(res).send(argument.capture());
                assertEquals(Response.OK, argument.getValue());

                return null;
            });

        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);
        when(address.getCountryCode()).thenReturn(1);

        when(registeredDelivery.getForwardDlrs()).thenReturn(true);

        handler.onSubmitSm(req, res);
    }

    @Test
    void submitSmCanPersistDlrToDatabase() {
        // tell Dalia message was sent successfully
        wsd.when(() -> WsdServer.sendNext(any(WsdMessage.class), any(WsdStatusListener.class)))
            .then(invocation -> {
                Object[] args = invocation.getArguments();
                WsdStatusListener listener = (WsdStatusListener) args[1];
                listener.onStatus(WsdResponseCode.SUCCESS);
                return null;
            });

        when(address.getSupported()).thenReturn(true);
        when(address.isValidNpi()).thenReturn(true);
        when(address.isValidTon()).thenReturn(true);

        when(registeredDelivery.getForwardDlrs()).thenReturn(true);

        ResponseSender res = mock(ResponseSender.class);
        handler.onSubmitSm(req, res);
        verify(database, times(1)).fetch(anyString(), any(Map.class));
        verify(database, times(2)).insert(anyString(), any(Map.class));

        wsd.verify(() -> WsdServer.sendNext(any(WsdMessage.class), any(WsdStatusListener.class)));
    }
}