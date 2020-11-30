package com.modulytic.dalia.smpp.include;

import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SmppRequestRouter {
    private DaliaSmppSessionListener listener = null;
    private SmppAuthenticator auth = null;

    protected String smppUser;
    protected final Logger LOGGER = LoggerFactory.getLogger(SmppRequestRouter.class);

    public void setListener(DaliaSmppSessionListener listener) {
        this.listener = listener;
    }

    public void setAuthenticator(SmppAuthenticator authenticator) {
        this.auth = authenticator;
    }

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
            response = this.onSubmitSm((SubmitSm) req);
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

    private Response onBind(Bind bind) {
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

    private Response onUnBind() {
        return Response.OK;
    }

    // this is to keep the connection open
    private Response onEnquireLink() {
        return Response.OK;
    }

    public abstract void onAuthSuccess(String sysId);
    public abstract void onAuthFailure(String sysId);

    public abstract Response onSubmitSm(SubmitSm submitSm);
    public abstract Response onCancelSm(SmppRequest cancelSm);
    public abstract Response onQuerySm(SmppRequest querySm);
    public abstract Response onReplaceSm(SmppRequest replaceSm);
    public abstract Response onSubmitMulti(SmppRequest submitMulti);
}
