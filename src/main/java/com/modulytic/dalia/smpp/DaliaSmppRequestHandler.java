package com.modulytic.dalia.smpp;

import com.modulytic.dalia.billing.Vroute;
import com.modulytic.dalia.billing.BillingManager;
import com.modulytic.dalia.local.include.DbManager;
import com.modulytic.dalia.smpp.request.SubmitRequest;
import com.modulytic.dalia.smpp.api.SMSCAddress;
import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import com.modulytic.dalia.ws.WsdServer;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.packet.SmppRequest;

/**
 * Handle PDUs and distribute incoming SMSes
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DaliaSmppRequestHandler extends SmppRequestHandler {
    /**
     * Active database connection
     */
    private final DbManager database;

    private WsdServer wsdServer;

    /**
     * Constructor
     * @param database  Active database connection
     */
    public DaliaSmppRequestHandler(DbManager database) {
        super();

        this.database = database;
    }

    /**
     * Pass WebSocket server to request router so we can send client messages
     * @param server    WebSocket server
     */
    public void setWsdServer(WsdServer server) {
        this.wsdServer = server;
    }

    @Override
    public void onAuthSuccess(String sysId) {
    }

    @Override
    public void onAuthFailure(String sysId) {
    }

    // TODO handle replace if present on submit_sm
    @Override
    public Response onSubmitSm(SubmitRequest submitSm) {
        Response response = Response.OK;
        response.setMessageId(submitSm.getMessageId());

        // Parse destination phone number, and pass errors to client
        SMSCAddress dest = submitSm.getDestAddress();
        if (!dest.isValidNpi()) {
            return Response.INVALID_DESTINATION_NPI;
        }
        else if (!dest.isValidTon()) {
            return Response.INVALID_DESTINATION_TON;
        }
        else if (!dest.isSupported()) {
            return Response.INVALID_DEST_ADDRESS;
        }

        // save message to our database for billing purposes
        BillingManager billingManager = new BillingManager(this.database);
        Vroute vroute = billingManager.getActiveVroute(dest.getCountryCode());
        billingManager.logMessage(submitSm.getMessageId(), getSmppUser(), dest.getCountryCode(), vroute);

        // no clients are available to take the message
        boolean sendSuccess = wsdServer.sendNext(submitSm.toEndpointRequest());
        if (sendSuccess) {
            if (submitSm.getShouldForwardDLRs())
                submitSm.persistDLRParamsTo(this.database);
        }
        else {
            return Response.SYSTEM_ERROR;
        }

        // TODO if intermediate DLRs requested, send accepted/en_route

        return response;
    }

    @Override
    public Response onCancelSm(SmppRequest cancelSm) {
        return Response.OK;
    }

    /**
     * If message is in DLR database, handle properly, otherwise generic_nack
     * @param querySm   query_sm PDU
     * @return          {@link Response SMPP response}
     */
    @Override
    public Response onQuerySm(SmppRequest querySm) {
        return Response.OK;
    }

    @Override
    public Response onReplaceSm(SmppRequest replaceSm) {
        return Response.OK;
    }

    @Override
    public Response onSubmitMulti(SmppRequest submitMulti) {
        return Response.OK;
    }
}
