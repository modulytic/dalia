package com.modulytic.dalia.smpp.include;

import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
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
    private DaliaSmppSessionListener listener = null;
    private SmppAuthenticator auth = null;
    private String smppUser;

    protected final Logger LOGGER = LoggerFactory.getLogger(SmppRequestHandler.class);

    /**
     * Setter for DaliaSmppSessionListener, so we can know which sessions are connected and access them
     * @param listener  DaliaSmppSessionListener
     */
    public void setListener(DaliaSmppSessionListener listener) {
        this.listener = listener;
    }

    /**
     * Setter for SMPP authenticator, credentials are passed to this for a response
     * @param authenticator ? extends SmppAuthenticator
     */
    public void setAuthenticator(SmppAuthenticator authenticator) {
        this.auth = authenticator;
    }

    /**
     * Handler for any incoming SMPP packets
     * @param req   SmppRequest, incoming packet
     * @param res   ResponseSender, can send a Response object to ESME
     */
    public void onSmppRequest(SmppRequest req, ResponseSender res) {
        // make sure listener is defined
        if (this.listener == null) {
            LOGGER.error("Fatal error: No listener specified in request router!!!");
            res.send(Response.SYSTEM_ERROR);
            return;
        }

        final Response response;

        if (req.isBind()) {
            response = this.onBind((Bind) req);
        }
        else if (req.getCommandId() == SmppPacket.UNBIND) {
            response = this.onUnBind();
        }
        else if (req.getCommandId() == SmppPacket.ENQUIRE_LINK) {
            response = this.onEnquireLink();
        }
        else if (req.isSubmitSm()) {
            SubmitRequest submitRequest = new SubmitRequest((SubmitSm) req);
            submitRequest.setSmppUser(this.smppUser);

            response = this.onSubmitSm(submitRequest);
        }
        else if (req.getCommandId() == SmppPacket.CANCEL_SM) {
            response = this.onCancelSm(req);
        }
        else if (req.getCommandId() == SmppPacket.QUERY_SM) {
            response = this.onQuerySm(req);
        }
        else if (req.getCommandId() == SmppPacket.REPLACE_SM) {
            response = this.onReplaceSm(req);
        }
        else if (req.getCommandId() == SmppPacket.SUBMIT_MULTI) {
            response = this.onSubmitMulti(req);
        }
        else {
            response = Response.INVALID_COMMAND_ID;
        }

        if (response != null) {
            res.send(response);
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
     * @return      Response from authenticator, or failure in certain cases
     */
    public Response onBind(Bind bind) {
        // if no authenticator, always fail
        if (this.auth == null) {
            LOGGER.error("Cannot authenticate user: no authenticator defined in request router!!!");
            return Response.BIND_FAILED;
        }

        final String systemId = bind.getSystemId();
        this.smppUser = systemId;

        // do not attempt authentication if already bound
        if (this.listener.getSessionBridge(systemId) != null)
            return Response.ALREADY_BOUND;

        Response authResponse = this.auth.auth(systemId, bind.getPassword());
        if (authResponse == Response.OK) {
            // let the rest of the system know we are successfully bound
            this.listener.activateSession(systemId);
            onAuthSuccess(systemId);
        }
        else {
            onAuthFailure(systemId);
        }

        return authResponse;
    }

    /**
     * Handle disconnect from ESME
     * @return  {@link Response Response.OK}
     */
    private Response onUnBind() {
        return Response.OK;
    }

    /**
     * Keep connection open by acknowledging enquire_link PDUs
     * @return {@link Response Response.OK}
     */
    private Response onEnquireLink() {
        return Response.OK;
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
     * @return  {@link Response Response}
     */
    public abstract Response onSubmitSm(SubmitRequest submitSm);

    /**
     * Handle cancel_sm PDU
     * @param cancelSm  cancel_sm PDU
     * @return  {@link Response Response}
     */
    public abstract Response onCancelSm(SmppRequest cancelSm);

    /**
     * Handle query_sm PDU
     * @param querySm  query_sm PDU
     * @return  {@link Response Response}
     */
    public abstract Response onQuerySm(SmppRequest querySm);

    /**
     * Handle replace_sm PDU
     * @param replaceSm  replace_sm PDU
     * @return  {@link Response Response}
     */
    public abstract Response onReplaceSm(SmppRequest replaceSm);

    /**
     * Handle submit_multi PDU
     * @param submitMulti  submit_multi PDU
     * @return  {@link Response Response}
     */
    public abstract Response onSubmitMulti(SmppRequest submitMulti);
}
