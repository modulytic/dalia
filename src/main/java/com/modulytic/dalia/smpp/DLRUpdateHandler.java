package com.modulytic.dalia.smpp;

import com.modulytic.dalia.local.MySqlDbManager;
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
public class DLRUpdateHandler {
    protected final Logger LOGGER = LoggerFactory.getLogger(WsdServer.class);

    /**
     * Active database connection
     */
    private final MySqlDbManager database;

    private final DaliaSmppSessionListener listener;
    public DLRUpdateHandler(MySqlDbManager database, DaliaSmppSessionListener listener) {
        this.database = database;
        this.listener = listener;
    }

    /**
     * Set and send a new status for a message where DLRs were requested
     * @param messageId message assigned when submit_sm was received
     * @param status    new {@link MessageState status}
     */
    public void updateStatus(String messageId, MessageState status) {
        DLRRequest request = new DLRRequest(messageId, this.database);
        if (request.existsInDb()) {
            request.persistNewStatus(status);

            if (request.clientWantsUpdate()) {
                DaliaSessionBridge bridge = request.getBridge(this.listener);

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
