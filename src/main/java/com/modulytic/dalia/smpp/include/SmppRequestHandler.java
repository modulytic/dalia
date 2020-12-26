package com.modulytic.dalia.smpp.include;

import com.modulytic.dalia.DaliaContext;
import com.modulytic.dalia.smpp.request.SubmitRequest;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract router class for incoming SMPP packets
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public abstract class SmppRequestHandler {
    private SmppAuthenticator authenticator;
    private String smppUser;

    protected static final Logger LOGGER = LoggerFactory.getLogger(SmppRequestHandler.class);

    /**
     * Setter for SMPP authenticator, credentials are passed to this for a response
     * @param auth ? extends SmppAuthenticator
     */
    public void setAuthenticator(SmppAuthenticator auth) {
        this.authenticator = auth;
    }

    /**
     * Handler for any incoming SMPP packets
     * @param req   SmppRequest, incoming packet
     * @param res   ResponseSender, can send a Response object to ESME
     */
    public void onSmppRequest(SmppRequest req, ResponseSender res) {
        // make sure listener is defined
        if (DaliaContext.getSessionListener() == null) {
            LOGGER.error("Fatal error: No listener specified in request router!!!");
            res.send(Response.SYSTEM_ERROR);
            return;
        }

        if (req.isBind()) {
            this.onBind((Bind) req, res);
        }
        else if (req.isSubmitSm()) {
            SubmitRequest submitRequest = new SubmitRequest((SubmitSm) req);
            submitRequest.setSmppUser(this.smppUser);

            this.onSubmitSm(submitRequest, res);
        }
        else switch (req.getCommandId()) {
            case SmppPacket.UNBIND:
                this.onUnBind(res);
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

    public String getSmppUser() {
        return this.smppUser;
    }

    /**
     * Handler for bind requests from ESME
     *
     * <p>
     * Returns failure if {@link #setAuthenticator(SmppAuthenticator)} setAuthenticator} not called, or if
     * the requested user is already bound
     * </p>
     *
     * @param bind  bind packet
     */
    private void onBind(Bind bind, ResponseSender responseSender) {
        // if no authenticator, always fail
        if (authenticator == null) {
            LOGGER.error("Cannot authenticate user: no authenticator defined in request router!!!");
            responseSender.send(Response.BIND_FAILED);
            return;
        }

        final String systemId = bind.getSystemId();
        this.smppUser = systemId;

        // do not attempt authentication if already bound
        if (DaliaContext.getSessionListener().getSessionBridge(systemId) != null) {
            responseSender.send(Response.ALREADY_BOUND);
            return;
        }

        Response authResponse = this.authenticator.auth(systemId, bind.getPassword());
        if (authResponse == Response.OK) {
            // let the rest of the system know we are successfully bound
            DaliaContext.getSessionListener().activateSession(systemId);
            onAuthSuccess(systemId);
        }
        else {
            onAuthFailure(systemId);
        }

        responseSender.send(authResponse);
    }

    /**
     * Handle disconnect from ESME
     */
    private void onUnBind(ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    /**
     * Keep connection open by acknowledging enquire_link PDUs
     */
    private void onEnquireLink(ResponseSender responseSender) {
        responseSender.send(Response.OK);
    }

    /**
     * Handle successful authentication from SMPP
     * @param sysId username that was authenticated
     */
    public abstract void onAuthSuccess(String sysId);

    /**
     * Handle failed authentication from SMPP
     * @param sysId username from the request
     */
    public abstract void onAuthFailure(String sysId);

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
