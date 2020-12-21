package com.modulytic.dalia.smpp;

import com.modulytic.dalia.billing.Vroute;
import com.modulytic.dalia.billing.BillingManager;
import com.modulytic.dalia.local.include.DbManager;
import com.modulytic.dalia.smpp.api.MessageState;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.request.SubmitRequest;
import com.modulytic.dalia.smpp.api.SMSCAddress;
import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessageCode;
import com.modulytic.dalia.ws.include.WsdStatusListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
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

    private DLRUpdateHandler updateHandler;

    /**
     * Constructor
     * @param database  Active database connection
     */
    public DaliaSmppRequestHandler(DbManager database) {
        super();

        this.database = database;
    }

    public void setUpdateHandler(DLRUpdateHandler handler) {
        this.updateHandler = handler;
    }

    private void updateMessageStatus(String id, RegisteredDelivery registeredDelivery, MessageState newStatus) {
        if (id == null || registeredDelivery == null || newStatus == null)
            return;

        if (!registeredDelivery.getForwardDlrs())
            return;

        if (this.updateHandler == null)
            return;

        if (registeredDelivery.getFailureOnly() && !MessageState.isError(newStatus))
            return;
        else if (!registeredDelivery.getIntermediate() && !MessageState.isFinal(newStatus))
            return;
        else if (!registeredDelivery.getReceiveFinal() && MessageState.isFinal(newStatus))
            return;

        this.updateHandler.updateStatus(id, newStatus);
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
    public void onSubmitSm(SubmitRequest submitSm, ResponseSender responseSender) {
        final Response response = Response.OK;
        response.setMessageId(submitSm.getMessageId());

        // Parse destination phone number, and pass errors to client
        SMSCAddress dest = submitSm.getDestAddress();
        if (!dest.isValidNpi()) {
            responseSender.send(Response.INVALID_DESTINATION_NPI);
            return;
        }
        else if (!dest.isValidTon()) {
            responseSender.send(Response.INVALID_DESTINATION_TON);
            return;
        }
        else if (!dest.isSupported()) {
            responseSender.send(Response.INVALID_DEST_ADDRESS);
            return;
        }

        final RegisteredDelivery registeredDelivery = new RegisteredDelivery(submitSm.getRegisteredDelivery());

        // save message to our database for billing purposes
        BillingManager billingManager = new BillingManager(this.database);
        Vroute vroute = billingManager.getActiveVroute(dest.getCountryCode());
        billingManager.logMessage(submitSm.getMessageId(), getSmppUser(), dest.getCountryCode(), vroute);

        // no clients are available to take the message
        WsdStatusListener listener = new WsdStatusListener() {
            @Override
            public void onStatus(int status) {
                if (status == WsdMessageCode.SUCCESS) {
                    LOGGER.info(String.format("Message '%s' successfully sent", submitSm.getMessageId()));
                    if (submitSm.getShouldForwardDLRs()) {
                        submitSm.persistDLRParamsTo(database);

                        // if intermediate DLRs requested, send accepted/en_route
                        updateMessageStatus(submitSm.getMessageId(), registeredDelivery, MessageState.EN_ROUTE);
                    }

                    responseSender.send(response);
                }
                else if (status == WsdMessageCode.NO_CLIENTS) {
                    LOGGER.error(String.format("Attempted to send message '%s', but no clients connected", submitSm.getMessageId()));
                    responseSender.send(Response.SYSTEM_ERROR);
                }
                else {
                    LOGGER.error(String.format("Message '%s' failed to be sent, error code: %d", submitSm.getMessageId(), status));
                    responseSender.send(Response.SYSTEM_ERROR);
                }
            }
        };

        wsdServer.sendNext(submitSm.toEndpointRequest(), listener);
    }

    @Override
    public void onCancelSm(SmppRequest cancelSm, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    /**
     * If message is in DLR database, handle properly, otherwise generic_nack
     * @param querySm   query_sm PDU
     */
    @Override
    public void onQuerySm(SmppRequest querySm, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    @Override
    public void onReplaceSm(SmppRequest replaceSm, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    @Override
    public void onSubmitMulti(SmppRequest submitMulti, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }
}
