package com.modulytic.dalia.ws.api;

import com.google.gson.Gson;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.smpp.internal.PduBridge;
import net.gescobar.smppserver.packet.CancelSm;
import net.gescobar.smppserver.packet.GenericSubmit;
import net.gescobar.smppserver.packet.ReplaceSm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ws-daemon message, handled by Gson
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class WsdMessage {
    /**
     * name field, specifies action to take
     */
    private final String name;

    /**
     * ID of ws-daemon message, NOT SMS!
     */
    private final String id;

    /**
     * Params passed to command
     */
    private final Map<String, ?> params;

    /**
     * Create new message with given name and params, and randomly-generated ID
     * @param name      name of script to trigger on endpoint
     * @param params    params to pass to endpoint
     */
    public WsdMessage(String name, Map<String, ?> params) {
        this.name = name;
        this.params = params;
        this.id = UUID.randomUUID().toString();
    }

    public static WsdMessage fromCancelSm(PduBridge<CancelSm> sm) {
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put(WsdMessageConstants.DESTINATION, sm.getDestAddress().toE164());
        params.put(WsdMessageConstants.MESSAGE_ID,  sm.getPdu().getMessageId());

        return new WsdMessage(WsdMessageConstants.Scripts.CANCEL, params);
    }

    /**
     * build params to forward request to endpoint
     * @return  {@link WsdMessage}
     */
    // TODO make WsdMessageConstants and move theses string literals there (and unfuck DatabaseConstants)
    public static WsdMessage fromSubmitPdu(PduBridge<? extends GenericSubmit> sm, List<AppAddress> dests, String messageId) {
        final GenericSubmit pdu = sm.getPdu();
        final RegisteredDelivery registeredDelivery = sm.getRegisteredDelivery();

        List<String> e164Dests = new ArrayList<>();
        for (AppAddress dest : dests) {
            e164Dests.add(dest.toE164());
        }

        Map<String, Object> sendParams = new ConcurrentHashMap<>();
        sendParams.put(WsdMessageConstants.DESTINATION,    e164Dests);
        sendParams.put(WsdMessageConstants.MESSAGE_ID,     messageId);
        sendParams.put(WsdMessageConstants.FORWARD_DLR,    registeredDelivery.getForwardDlrs());
        sendParams.put(WsdMessageConstants.DELIVERY_TIME,  pdu.getScheduleDeliveryTime());
        sendParams.put(WsdMessageConstants.VALID_FOR,      pdu.getValidityPeriod());
        sendParams.put(WsdMessageConstants.CONTENT,        pdu.getShortMessage());

        return new WsdMessage(WsdMessageConstants.Scripts.SEND, sendParams);
    }

    public static WsdMessage fromReplaceSm(PduBridge<ReplaceSm> sm) {
        final ReplaceSm pdu = sm.getPdu();

        Map<String, Object> sendParams = new ConcurrentHashMap<>();
        sendParams.put(WsdMessageConstants.MESSAGE_ID,     pdu.getMessageId());
        sendParams.put(WsdMessageConstants.FORWARD_DLR,    sm.getRegisteredDelivery().getForwardDlrs());
        sendParams.put(WsdMessageConstants.DELIVERY_TIME,  pdu.getScheduleDeliveryTime());
        sendParams.put(WsdMessageConstants.VALID_FOR,      pdu.getValidityPeriod());
        sendParams.put(WsdMessageConstants.CONTENT,        pdu.getShortMessage());

        return new WsdMessage(WsdMessageConstants.Scripts.REPLACE, sendParams);
    }

    /**
     * get name field
     * @return  name in string format
     */
    public String getName() {
        return this.name;
    }

    /**
     * get ID of message
     * @return  ID, string
     */
    public String getId() {
        return this.id;
    }

    /**
     * get params field
     * @return  map of params
     */
    public Map<String, ?> getParams() {
        return this.params;
    }

    /**
     * Convert message to string for sending over WebSockets
     * @return  string in JSON format
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
