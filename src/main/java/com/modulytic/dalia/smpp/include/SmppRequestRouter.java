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

    protected final Logger log = LoggerFactory.getLogger(SmppRequestRouter.class);

    public SmppRequestRouter(SmppAuthenticator authenticator, DaliaSmppSessionListener listener) {
        this.auth = authenticator;
        this.listener = listener;
    }

    public void route(SmppRequest req, ResponseSender res) {
        final Response response;

        if (req.isBind()) {
            response = this.handleBind((Bind)req);
        }
        else if (req.getCommandId() == SmppPacket.ENQUIRE_LINK) {
            response = this.handleEnquireLink();
        }
        else if (req.isSubmitSm()) {
            response = this.handleSubmitSm((SubmitSm)req);
        }
        else if (req.getCommandId() == SmppPacket.CANCEL_SM) {
            response = this.handleCancelSm(req);
        }
        else if (req.getCommandId() == SmppPacket.QUERY_SM) {
            response = this.handleQuerySm(req);
        }
        else if (req.getCommandId() == SmppPacket.REPLACE_SM) {
            response = this.handleReplaceSm(req);
        }
        else {
            response = Response.INVALID_COMMAND_ID;
        }

        if (response != null) {
            res.send(response);
        }
    }

    private Response handleBind(Bind bind) {
        final String systemId = bind.getSystemId();

        boolean authenticated = this.auth.auth(systemId, bind.getPassword());
        if (authenticated) {
            // let the rest of the system know we are successfully bound
            this.listener.activateSession(systemId);

            handleAuthSuccess(systemId);
            return Response.OK;
        }
        else {
            handleAuthFailure(systemId);
            return Response.BIND_FAILED;
        }
    }

    private Response handleEnquireLink() {
        return Response.OK;
    }

    public abstract void handleAuthSuccess(String sysId);
    public abstract void handleAuthFailure(String sysId);

    public abstract Response handleSubmitSm(SubmitSm submitSm);
    public abstract Response handleCancelSm(SmppRequest cancelSm);
    public abstract Response handleQuerySm(SmppRequest querySm);
    public abstract Response handleReplaceSm(SmppRequest replaceSm);
}
