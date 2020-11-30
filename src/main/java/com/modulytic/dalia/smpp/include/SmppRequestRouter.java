package com.modulytic.dalia.smpp.include;

import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SmppRequestRouter {
    private final DaliaSmppSessionListener listener;
    private final SmppAuthenticator auth;

    protected String smppUser;
    protected final Logger LOGGER = LoggerFactory.getLogger(SmppRequestRouter.class);

    public SmppRequestRouter(SmppAuthenticator authenticator, DaliaSmppSessionListener listener) {
        this.auth = authenticator;
        this.listener = listener;
    }

    public void onSmppRequest(SmppRequest req, ResponseSender res) {
        final Response response;

        if (req.isBind()) {
            response = this.onBind((Bind)req);
        }
        else if (req.getCommandId() == SmppPacket.ENQUIRE_LINK) {
            response = this.onEnquireLink();
        }
        else if (req.isSubmitSm()) {
            response = this.onSubmitSm((SubmitSm)req);
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
        final String systemId = bind.getSystemId();
        this.smppUser = systemId;

        boolean authenticated = this.auth.auth(systemId, bind.getPassword());
        if (authenticated) {
            // let the rest of the system know we are successfully bound
            this.listener.activateSession(systemId);

            onAuthSuccess(systemId);
            return Response.OK;
        }
        else {
            onAuthFailure(systemId);
            return Response.BIND_FAILED;
        }
    }

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
