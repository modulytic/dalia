package com.modulytic.dalia.smpp.include;

import com.modulytic.dalia.smpp.event.DaliaSmppSessionListener;
import com.modulytic.dalia.smpp.request.SubmitRequest;
import net.gescobar.smppserver.PacketProcessor;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract router class for incoming SMPP packets
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public abstract class SmppRequestHandler implements PacketProcessor {
    private static SmppAuthenticator authenticator;
    private String smppUser;

    protected static final Logger LOGGER = LoggerFactory.getLogger(SmppRequestHandler.class);

    /**
     * Handler for bind requests from ESME
     * @param bind  bind packet
     */
    private static boolean authBind(Bind bind, ResponseSender responseSender) {
        final String systemId = bind.getSystemId();

        // do not attempt authentication if already bound
        if (DaliaSmppSessionListener.getSessionBridge(systemId) != null) {
            responseSender.send(Response.ALREADY_BOUND);
            return false;
        }

        Response authResponse = authenticator.auth(systemId, bind.getPassword());
        responseSender.send(authResponse);

        return (authResponse == Response.OK);
    }

    /**
     * Setter for SMPP authenticator, credentials are passed to this for a response
     * @param auth ? extends SmppAuthenticator
     */
    public static void setAuthenticator(SmppAuthenticator auth) {
        authenticator = auth;
    }

    /**
     * Handler for any incoming SMPP packets
     * @param req   SmppRequest, incoming packet
     * @param res   ResponseSender, can send a Response object to ESME
     */
    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")       // needed for routing
    public void processPacket(SmppRequest req, ResponseSender res) {
        switch (req.getCommandId()) {
            case SmppPacket.BIND_TRANSMITTER:
            case SmppPacket.BIND_RECEIVER:
            case SmppPacket.BIND_TRANSCEIVER:
                Bind b = (Bind) req;
                String sysId = b.getSystemId();

                if (authBind(b, res)) {
                    setSmppUser(sysId);
                    DaliaSmppSessionListener.activate(sysId);
                }

                break;

            case SmppPacket.UNBIND:
                this.onUnbind(res);
                break;

            case SmppPacket.SUBMIT_SM:
                SubmitRequest submitRequest = new SubmitRequest((SubmitSm) req);
                submitRequest.setSmppUser(getSmppUser());

                this.onSubmitSm(submitRequest, res);
                break;

            case SmppPacket.ENQUIRE_LINK:
                this.onEnquireLink(res);
                break;

            case SmppPacket.CANCEL_SM:
                this.onCancelSm(req, res);
                break;

            case SmppPacket.QUERY_SM:
                this.onQuerySm(req, res);
                break;

            case SmppPacket.REPLACE_SM:
                this.onReplaceSm(req, res);
                break;

            case SmppPacket.SUBMIT_MULTI:
                this.onSubmitMulti(req, res);
                break;

            default:
                res.send(Response.INVALID_COMMAND_ID);
                break;
        }
    }

    private void onUnbind(ResponseSender res) {
        DaliaSmppSessionListener.deactivate(getSmppUser());
        res.send(Response.OK);
    }

    public void setSmppUser(String smppuser) {
        this.smppUser = smppuser;
    }

    public String getSmppUser() {
        return this.smppUser;
    }

    /**
     * Keep connection open by acknowledging enquire_link PDUs
     */
    private void onEnquireLink(ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    /**
     * Handle submit_sm PDU
     * @param submitSm  submit_sm PDU
     */
    public abstract void onSubmitSm(SubmitRequest submitSm, ResponseSender responseSender);

    /**
     * Handle cancel_sm PDU
     * @param cancelSm  cancel_sm PDU
     */
    public abstract void onCancelSm(SmppRequest cancelSm, ResponseSender responseSender);

    /**
     * Handle query_sm PDU
     * @param querySm  query_sm PDU
     */
    public abstract void onQuerySm(SmppRequest querySm, ResponseSender responseSender);

    /**
     * Handle replace_sm PDU
     * @param replaceSm  replace_sm PDU
     */
    public abstract void onReplaceSm(SmppRequest replaceSm, ResponseSender responseSender);

    /**
     * Handle submit_multi PDU
     * @param submitMulti  submit_multi PDU
     */
    public abstract void onSubmitMulti(SmppRequest submitMulti, ResponseSender responseSender);
}
