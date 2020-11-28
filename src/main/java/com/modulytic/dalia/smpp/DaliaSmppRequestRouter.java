package com.modulytic.dalia.smpp;

import com.modulytic.dalia.smpp.include.SmppAuthenticator;
import com.modulytic.dalia.smpp.include.SmppRequestRouter;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SubmitSm;

import java.util.UUID;

public class DaliaSmppRequestRouter extends SmppRequestRouter {
    public DaliaSmppRequestRouter(SmppAuthenticator authenticator, DaliaSmppSessionListener listener) {
        super(authenticator, listener);
    }

    @Override
    public void handleAuthSuccess(String sysId) {
    }

    @Override
    public void handleAuthFailure(String sysId) {

    }

    @Override
    public Response handleSubmitSm(SubmitSm submitSm) {
        Response response = null;
        UUID messageId = UUID.randomUUID();

        response = Response.OK;
        response.setMessageId(messageId.toString());

        // TODO add to MySQL if DLRs are requested, and manage different
        //  settings for registered_delivery (see pg. 124 of SMPP spec)

        return response;
    }

    @Override
    public Response handleCancelSm(SmppRequest cancelSm) {
        return Response.OK;
    }

    @Override
    public Response handleQuerySm(SmppRequest querySm) {
        return Response.OK;
    }

    @Override
    public Response handleReplaceSm(SmppRequest replaceSm) {
        return Response.OK;
    }
}
