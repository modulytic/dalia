package com.modulytic.dalia.smpp.event;

import com.modulytic.dalia.app.database.SmPersister;
import com.modulytic.dalia.billing.Billing;
import com.modulytic.dalia.billing.Vroute;
import com.modulytic.dalia.smpp.api.MessageState;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import com.modulytic.dalia.smpp.internal.DLRRequest;
import com.modulytic.dalia.smpp.internal.PduBridge;
import com.modulytic.dalia.smpp.internal.SmppTime;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessage;
import com.modulytic.dalia.ws.api.WsdResponseCode;
import com.modulytic.dalia.ws.include.WsdStatusListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handle PDUs and distribute incoming SMSes
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class DaliaSmppRequestHandler extends SmppRequestHandler {
    @Override
    public void onSubmitSm(PduBridge<SubmitSm> submitSm, ResponseSender responseSender) {
        final List<AppAddress> address = new ArrayList<>();
        address.add(submitSm.getDestAddress());

        submit(submitSm, address, responseSender);
    }

    @Override
    public void onCancelSm(PduBridge<CancelSm> cancelSm, ResponseSender responseSender) {
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

        WsdMessage message = WsdMessage.fromCancelSm(cancelSm);
        WsdServer.sendToNextEndpoint(message, listener);
    }

    /**
     * If message is in DLR database, handle properly, otherwise generic_nack
     * @param querySm   query_sm PDU
     */
    // TODO make the error code not always zero
    @Override
    public void onQuerySm(PduBridge<QuerySm> querySm, ResponseSender responseSender) {
        final String messageId = querySm.getPdu().getMessageId();

        DLRRequest search = new DLRRequest(messageId);
        if (search.existsInDb()) {
            // make sure we don't leak other people's data, and make sure source address is the same
            final String source = querySm.getSourceAddress().getAddress();
            if (getSmppUser().equals(search.getSmppUser()) && source.equals(search.getSourceAddress())) {
                QuerySmResp res = new QuerySmResp();

                MessageState messageState = search.getMessageState();

                // final_date is NULL unless the status is actually final
                String finalDate = null;
                if (MessageState.isFinal(messageState))
                    finalDate = SmppTime.formatAbsolute(search.getUpdateTime());

                res.setCommandStatus(Response.OK.getCommandStatus());
                res.setMessageId(messageId);
                res.setFinalDate(finalDate);
                res.setMessageState(messageState.toSmpp());
                res.setErrorCode((byte) 0);

                sendPdu(res);
            }
        }
        else {
            responseSender.send(Response.QUERY_SM_FAILED);
        }
    }

    @Override
    public void onReplaceSm(PduBridge<ReplaceSm> replaceSm, ResponseSender responseSender) {
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
                    responseSender.send(Response.REPLACE_SM_FAILED);
                }
            }
        };

        WsdMessage message = WsdMessage.fromReplaceSm(replaceSm);
        WsdServer.sendToNextEndpoint(message, listener);
    }

    @Override
    public void onSubmitMulti(PduBridge<SubmitMulti> submitMulti, ResponseSender responseSender) {
        submit(submitMulti, submitMulti.getDestAddresses(), responseSender);
    }

    // TODO only log message in database if the message is actually successfully sent
    // TODO handle replace if present
    private void submit(PduBridge<? extends GenericSubmit> sm, List<AppAddress> dests, ResponseSender res) {
        final String messageId = UUID.randomUUID().toString();
        final Response response = Response.OK.withMessageId(messageId);

        // make sure the destinations are all valid
        for (AppAddress dest : dests) {
            if (!dest.isValidNpi()) {
                res.send(Response.INVALID_DESTINATION_NPI);
                return;
            }
            else if (!dest.isValidTon()) {
                res.send(Response.INVALID_DESTINATION_TON);
                return;
            }
            else if (!dest.getSupported()) {
                res.send(Response.INVALID_DEST_ADDRESS);
                return;
            }

            // if so, then save message to our database for billing purposes
            Vroute vroute = Vroute.getActiveVroute(dest.getCountryCode());
            Billing.logMessage(messageId, getSmppUser(), dest.getCountryCode(), vroute);
        }

        RegisteredDelivery registeredDelivery = sm.getRegisteredDelivery();
        WsdStatusListener listener = new WsdStatusListener() {
            @Override
            public void onStatus(int status) {
                if (status == WsdResponseCode.SUCCESS) {
                    LOGGER.info(String.format("Message '%s' successfully sent", messageId));

                    if (registeredDelivery.getForwardDlrs()) {
                        SmPersister.save(sm, dests, messageId, getSmppUser());

                        // if intermediate DLRs requested, send accepted/en_route
                        DLRUpdateHandler.conditionalUpdate(messageId, registeredDelivery, MessageState.ENROUTE);
                    }

                    res.send(response);
                }

                // no clients are available to take the message
                else if (status == WsdResponseCode.NO_CLIENTS) {
                    LOGGER.error(String.format("Attempted to send message '%s', but no clients connected", messageId));
                    res.send(Response.SYSTEM_ERROR);
                }

                else {
                    LOGGER.error(String.format("Message '%s' failed to be sent, error code: %d", messageId, status));
                    res.send(Response.SYSTEM_ERROR);
                }
            }
        };

        WsdMessage message = WsdMessage.fromSubmitPdu(sm, dests, messageId);
        WsdServer.sendToNextEndpoint(message, listener);
    }
}
