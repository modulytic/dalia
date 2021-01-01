package com.modulytic.dalia.ws;

import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.smpp.internal.PduBridge;
import com.modulytic.dalia.ws.api.WsdMessage;
import net.gescobar.smppserver.packet.CancelSm;
import net.gescobar.smppserver.packet.SubmitSm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WsdMessageConverter {
    private WsdMessageConverter() {}

    public static WsdMessage toMessage(PduBridge<CancelSm> cancelSm) {
        return toMessage(cancelSm.getPdu());
    }

    public static WsdMessage toMessage(CancelSm cancelSm) {
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("msg_id", cancelSm.getMessageId());

        return new WsdMessage("cancel.php", params);
    }

    /**
     * build params to forward request to endpoint
     * @return  {@link WsdMessage}
     */
    // TODO make WsdMessageConstants and move theses string literals there (and unfuck DatabaseConstants)
    public static WsdMessage toMessage(PduBridge<SubmitSm> submitSm, String messageId) {
        final SubmitSm pdu = submitSm.getPdu();
        final AppAddress address = submitSm.getDestAddress();
        final RegisteredDelivery registeredDelivery = submitSm.getRegisteredDelivery();

        Map<String, Object> sendParams = new ConcurrentHashMap<>();
        sendParams.put("to", address.toE164());
        sendParams.put("content", pdu.getShortMessage());
        sendParams.put("id", messageId);
        sendParams.put("dlr", registeredDelivery.getForwardDlrs());
        sendParams.put("schedule_delivery_time", pdu.getScheduleDeliveryTime());
        sendParams.put("validity_period", pdu.getValidityPeriod());

        return new WsdMessage("send.php", sendParams);
    }
}
