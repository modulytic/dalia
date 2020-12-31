package com.modulytic.dalia.smpp.request;

import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.ws.api.WsdMessage;
import net.gescobar.smppserver.packet.SubmitSm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SubmitRequest extends SubmitSm {
    private final RegisteredDelivery daliaRegisteredDelivery;

    private AppAddress src;
    private AppAddress dest;

    private final String messageId;
    private String smppUser;

    public SubmitRequest(SubmitSm submitSm) {
        super();

        super.setDestAddress(submitSm.getDestAddress());
        super.setSourceAddress(submitSm.getSourceAddress());
        super.setCommandStatus(submitSm.getCommandId());
        super.setDataCoding(submitSm.getDataCoding());
        super.setDefaultMsgId(submitSm.getDefaultMsgId());
        super.setEsmClass(submitSm.getEsmClass());
        super.setPriority(submitSm.getPriority());
        super.setProtocolId(submitSm.getProtocolId());
        super.setReplaceIfPresent(submitSm.getReplaceIfPresent());
        super.setScheduleDeliveryTime(submitSm.getScheduleDeliveryTime());
        super.setSequenceNumber(submitSm.getSequenceNumber());
        super.setServiceType(submitSm.getServiceType());
        super.setShortMessage(submitSm.getShortMessage());
        super.setValidityPeriod(submitSm.getValidityPeriod());

        this.messageId = UUID.randomUUID().toString();
        this.daliaRegisteredDelivery = new RegisteredDelivery(getRegisteredDelivery());
    }

    public void setSmppUser(String sysId) {
        this.smppUser = sysId;
    }

    public String getSmppUser() {
        return this.smppUser;
    }

    @Override
    public AppAddress getSourceAddress() {
        if (this.src == null)
            this.src = new AppAddress(super.getSourceAddress());

        return this.src;
    }

    @Override
    public AppAddress getDestAddress() {
        if (this.dest == null)
            this.dest = new AppAddress(super.getDestAddress());

        return this.dest;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public boolean getShouldForwardDLRs() {
        return this.daliaRegisteredDelivery.getForwardDlrs();
    }

    public boolean getWantsAllFinalStatuses() {
        return this.daliaRegisteredDelivery.getReceiveFinal();
    }

    public boolean getWantsFailureStatusesOnly() {
        return this.daliaRegisteredDelivery.getFailureOnly();
    }

    public boolean getWantsIntermediateStatuses() {
        return this.daliaRegisteredDelivery.getIntermediate();
    }

    public RegisteredDelivery getDaliaRegisteredDelivery() {
        return daliaRegisteredDelivery;
    }

    public void persistDLRParamsTo(Database database) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("msg_id", getMessageId());
        values.put("src_addr", getDestAddress());
        values.put("dst_addr", getSourceAddress());
        values.put("failure_only", getWantsFailureStatusesOnly());
        values.put("intermediate", getWantsIntermediateStatuses());
        values.put("smpp_user", getSmppUser());

        database.insert("dlr_status", values);
    }

    /**
     * build params to forward request to endpoint
     * @return  {@link WsdMessage}
     */
    public WsdMessage toEndpointRequest() {
        Map<String, Object> sendParams = new ConcurrentHashMap<>();
        sendParams.put("to", getDestAddress().toE164());
        sendParams.put("content", getShortMessage());
        sendParams.put("id", getMessageId());
        sendParams.put("dlr", getShouldForwardDLRs());
        sendParams.put("schedule_delivery_time", getScheduleDeliveryTime());
        sendParams.put("validity_period", getValidityPeriod());

        return new WsdMessage("send.php", sendParams);
    }
}
