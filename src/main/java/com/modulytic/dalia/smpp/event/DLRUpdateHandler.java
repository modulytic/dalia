package com.modulytic.dalia.smpp.event;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.smpp.DaliaSessionBridge;
import com.modulytic.dalia.smpp.request.DLRRequest;
import com.modulytic.dalia.smpp.api.DeliveryReport;
import com.modulytic.dalia.smpp.api.MessageState;
import com.modulytic.dalia.ws.WsdServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the receipt of new message statuses from {@link WsdServer WebSockets}
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class DLRUpdateHandler {
    private DLRUpdateHandler() {}

    private static final transient Logger LOGGER = LoggerFactory.getLogger(WsdServer.class);

    /**
     * Set and send a new status for a message where DLRs were requested
     * @param messageId message assigned when submit_sm was received
     * @param status    new {@link MessageState status}
     */
    public static void updateStatus(String messageId, MessageState status) {
        if (Context.getDatabase() == null)      // for request.persistNewStatus
            return;

        DLRRequest request = new DLRRequest(messageId);
        if (request.existsInDb()) {
            request.persistNewStatus(status);

            if (request.clientWantsUpdate()) {
                DaliaSessionBridge bridge = request.getBridge();

                if (bridge != null) {
                    DeliveryReport deliveryReport = request.toDeliveryReport();
                    bridge.sendGescobarPdu(deliveryReport.toDeliverSm());
                }
            }
        }
        else {
            LOGGER.error(String.format("Received DLR update for message '%s', but it is not in the database", messageId));
        }
    }
}
