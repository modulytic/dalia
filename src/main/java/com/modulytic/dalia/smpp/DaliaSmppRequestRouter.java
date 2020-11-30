package com.modulytic.dalia.smpp;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.modulytic.dalia.billing.Vroute;
import com.modulytic.dalia.billing.BillingManager;
import com.modulytic.dalia.local.MySqlDbManager;
import com.modulytic.dalia.smpp.api.NPI;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.api.TON;
import com.modulytic.dalia.smpp.include.SmppAuthenticator;
import com.modulytic.dalia.smpp.include.SmppRequestRouter;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.api.WsdMessage;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SubmitSm;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class DaliaSmppRequestRouter extends SmppRequestRouter {
    private final MySqlDbManager database;
    private final PhoneNumberUtil phoneUtil;
    private WsdServer wsdServer;

    public DaliaSmppRequestRouter(SmppAuthenticator authenticator,
                                  DaliaSmppSessionListener listener,
                                  MySqlDbManager database) {
        super(authenticator, listener);

        this.phoneUtil = PhoneNumberUtil.getInstance();
        this.database = database;
    }

    public void setWsdServer(WsdServer server) {
        this.wsdServer = server;
    }

    @Override
    public void onAuthSuccess(String sysId) {
    }

    @Override
    public void onAuthFailure(String sysId) {

    }

    @Override
    public Response onSubmitSm(SubmitSm submitSm) {
        Response response = Response.OK;
        String messageId = UUID.randomUUID().toString();
        response.setMessageId(messageId);

        // Parse destination phone number
        Phonenumber.PhoneNumber destNumber;
        String destFormatted;
        try {
            Address destAddress = submitSm.getDestAddress();

            // make sure type of number is something we can handle
            byte ton = destAddress.getTon();
            if (ton != TON.INTERNATIONAL && ton != TON.NATIONAL)
                return Response.INVALID_DESTINATION_TON;

            // make sure numbering plan identification is something we can process
            byte npi = destAddress.getNpi();
            if (npi != NPI.E164 && npi != NPI.NATIONAL)
                return Response.INVALID_DESTINATION_NPI;

            destNumber = this.phoneUtil.parse(destAddress.getAddress(), "US");
            destFormatted = this.phoneUtil.format(destNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            return Response.INVALID_DEST_ADDRESS;
        }

        // if DLRs are requested, save relevant information for creating DeliverSM in database
        RegisteredDelivery registeredDelivery = new RegisteredDelivery(submitSm.getRegisteredDelivery());
        boolean shouldForwardDlrs = registeredDelivery.getForwardDlrs();
        if (shouldForwardDlrs) {
            String srcAddr  = submitSm.getSourceAddress().getAddress();

            // reverse src and dst because these are the details for future DLRs
            Map<String, Object> values = new TreeMap<>();
            values.put("msg_id", messageId);
            values.put("src_addr", destFormatted);
            values.put("dst_addr", srcAddr);
            values.put("failure_only", registeredDelivery.getFailureOnly());
            values.put("intermediate", registeredDelivery.getIntermediate());
            values.put("smpp_user", this.smppUser);

            this.database.insert("dlr_status", values);
        }

        // save message to our database for billing purposes
        int countryCode = destNumber.getCountryCode();
        BillingManager billingManager = new BillingManager(this.database);
        Vroute vroute = billingManager.getActiveVroute(countryCode);
        billingManager.logMessage(messageId, this.smppUser, countryCode, vroute);

        // build params to forward request to endpoint
        Map<String, Object> sendParams = new TreeMap<>();
        sendParams.put("to", destFormatted);
        sendParams.put("content", submitSm.getShortMessage());
        sendParams.put("id", messageId);
        sendParams.put("dlr", shouldForwardDlrs);
        sendParams.put("schedule_delivery_time", submitSm.getScheduleDeliveryTime());
        sendParams.put("validity_period", submitSm.getValidityPeriod());

        // forward request
        // TODO check status returned and fail if needed
        WsdMessage sendMessage = new WsdMessage("send.php", sendParams);
        boolean sendSuccess = wsdServer.sendNext(sendMessage);
        if (!sendSuccess)
            return Response.SYSTEM_ERROR;

        return response;
    }

    @Override
    public Response onCancelSm(SmppRequest cancelSm) {
        return Response.OK;
    }

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
