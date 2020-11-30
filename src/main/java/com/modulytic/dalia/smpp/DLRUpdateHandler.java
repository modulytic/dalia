package com.modulytic.dalia.smpp;

import com.modulytic.dalia.local.MySqlDbManager;
import com.modulytic.dalia.smpp.api.DeliveryReport;
import com.modulytic.dalia.smpp.api.MessageState;
import com.modulytic.dalia.ws.WsdServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class DLRUpdateHandler {
    protected final Logger LOGGER = LoggerFactory.getLogger(WsdServer.class);

    private final MySqlDbManager database;
    private final DaliaSmppSessionListener listener;
    public DLRUpdateHandler(MySqlDbManager database, DaliaSmppSessionListener listener) {
        this.database = database;
        this.listener = listener;
    }

    public void updateStatus(String messageId, String status) {
        try {
            updateStatusUnsafe(messageId, status);
        }
        catch (NullPointerException e) {
            LOGGER.error(String.format("Received DLR update for message '%s', but it is not in the database", messageId));
        }
    }

    private void updateStatusUnsafe(String messageId, String status) {
        if (!MessageState.isValid(status))
            return;

        Map<String, String> match = new TreeMap<>();
        match.put("msg_id", messageId);

        Map<String, String> values = new TreeMap<>();
        values.put("msg_status", status);

        this.database.update("dlr_status", values, match);

        // fetch params to build DLR and bridge to send it
        HashMap<String, ?> dlrParams = this.database.fetch("dlr_status", match).get(0);
        DaliaSessionBridge bridge = this.listener.getSessionBridge((String) dlrParams.get("smpp_user"));

        Boolean failureOnly  = (Boolean) dlrParams.get("failure_only");
        Boolean intermediate = (Boolean) dlrParams.get("intermediate");

        // if the client has requested this status update, send it
        // if neither failureOnly nor intermediate are set, it means the client wants all final statuses
        boolean statusIsFinal = MessageState.isFinal(status);
        if ((!failureOnly && statusIsFinal)
                    || (intermediate && !statusIsFinal)
                    || (failureOnly && MessageState.isError(status))) {
            // build DLR
            DeliveryReport deliveryReport = new DeliveryReport();
            deliveryReport.setId(messageId);
            deliveryReport.setSubmitDate((Timestamp) dlrParams.get("submit_date"));
            deliveryReport.setSourceAddr((String) dlrParams.get("src_addr"));
            deliveryReport.setDestAddr((String) dlrParams.get("dst_addr"));
            deliveryReport.setIsIntermediate(!statusIsFinal);
            deliveryReport.setStatus(status);

            // send to client
            bridge.sendPdu(deliveryReport.toDeliverSm());
        }

    }
}
