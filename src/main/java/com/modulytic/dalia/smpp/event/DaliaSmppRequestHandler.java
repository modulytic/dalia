package com.modulytic.dalia.smpp.event;

import com.modulytic.dalia.app.database.SmPersister;
import com.modulytic.dalia.billing.Billing;
import com.modulytic.dalia.billing.Vroute;
import com.modulytic.dalia.smpp.api.MessageState;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import com.modulytic.dalia.smpp.internal.PduBridge;
import com.modulytic.dalia.ws.WsdMessageConverter;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessage;
import com.modulytic.dalia.ws.api.WsdResponseCode;
import com.modulytic.dalia.ws.include.WsdStatusListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.*;

import java.util.UUID;

/**
 * Handle PDUs and distribute incoming SMSes
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class DaliaSmppRequestHandler extends SmppRequestHandler {
    // TODO handle replace if present on submit_sm
    @Override
    public void onSubmitSm(PduBridge<SubmitSm> submitSm, ResponseSender responseSender) {
        final String messageId = UUID.randomUUID().toString();
        final Response response = Response.OK.withMessageId(messageId);

        // Parse destination phone number, and pass errors to client
        final AppAddress dest = submitSm.getDestAddress();
        if (!dest.isValidNpi()) {
            responseSender.send(Response.INVALID_DESTINATION_NPI);
            return;
        }
        else if (!dest.isValidTon()) {
            responseSender.send(Response.INVALID_DESTINATION_TON);
            return;
        }
        else if (!dest.getSupported()) {
            responseSender.send(Response.INVALID_DEST_ADDRESS);
            return;
        }

        // save message to our database for billing purposes
        RegisteredDelivery registeredDelivery = submitSm.getRegisteredDelivery();
        Vroute vroute = Vroute.getActiveVroute(dest.getCountryCode());
        Billing.logMessage(messageId, getSmppUser(), dest.getCountryCode(), vroute);

        WsdStatusListener listener = new WsdStatusListener() {
            @Override
            public void onStatus(int status) {
                if (status == WsdResponseCode.SUCCESS) {
                    LOGGER.info(String.format("Message '%s' successfully sent", messageId));

                    if (registeredDelivery.getForwardDlrs()) {
                        SmPersister.save(submitSm, messageId, getSmppUser());

                        // if intermediate DLRs requested, send accepted/en_route
                        DLRUpdateHandler.conditionalUpdate(messageId, registeredDelivery, MessageState.EN_ROUTE);
                    }

                    responseSender.send(response);
                }

                // no clients are available to take the message
                else if (status == WsdResponseCode.NO_CLIENTS) {
                    LOGGER.error(String.format("Attempted to send message '%s', but no clients connected", messageId));
                    responseSender.send(Response.SYSTEM_ERROR);
                }

                else {
                    LOGGER.error(String.format("Message '%s' failed to be sent, error code: %d", messageId, status));
                    responseSender.send(Response.SYSTEM_ERROR);
                }
            }
        };

        WsdMessage message = WsdMessageConverter.toMessage(submitSm, messageId);
        WsdServer.sendNext(message, listener);
    }

    @Override
    public void onCancelSm(PduBridge<CancelSm> cancelSm, ResponseSender responseSender) {
        final CancelSm pdu = cancelSm.getPdu();
        final String messageId = pdu.getMessageId();

        if (messageId != null) {
            WsdStatusListener listener = new WsdStatusListener() {
                @Override
                public void onStatus(int status) {
                    if (status == WsdResponseCode.SUCCESS) {
                        responseSender.send(Response.OK);
                    }
                    else if (status == WsdResponseCode.ID_NOT_FOUND) {
                        responseSender.send(Response.INVALID_MESSAGE_ID);
                    }
                    else {
                        responseSender.send(Response.CANCEL_SM_FAILED);
                    }
                }
            };

            WsdMessage message = WsdMessageConverter.toMessage(cancelSm);
            WsdServer.sendNext(message, listener);
        }
        // todo, handle when messageId is null
    }

    /**
     * If message is in DLR database, handle properly, otherwise generic_nack
     * @param querySm   query_sm PDU
     */
    @Override
    public void onQuerySm(PduBridge<QuerySm> querySm, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    @Override
    public void onReplaceSm(PduBridge<ReplaceSm> replaceSm, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    @Override
    public void onSubmitMulti(PduBridge<SubmitMulti> submitMulti, ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }


}
