package com.modulytic.dalia.smpp.internal;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.DatabaseResults;
import com.modulytic.dalia.app.database.RowStore;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.app.database.include.DatabaseConstants;
import com.modulytic.dalia.smpp.DaliaSessionBridge;
import com.modulytic.dalia.smpp.event.DaliaSmppSessionListener;
import com.modulytic.dalia.smpp.api.DeliveryReport;
import com.modulytic.dalia.smpp.api.MessageState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Incoming request for a DLR update
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DLRRequest {
    /**
     * String of message request is for
     */
    private final String messageId;

    /**
     * DLR params
     */
    private RowStore values;

    /**
     * Map of message ID, saved in case of repeated requests
     */
    private Map<String, String> match;

    /**
     * Build DLR params from database
     * @param messageId ID of message assigned when submitted
     */
    public DLRRequest(String messageId) {
        this.messageId = messageId;

        final Database database = Context.getDatabase();
        final DatabaseResults values = database.fetch(DatabaseConstants.DLR_TABLE, getMatch());

        if (!values.isEmpty()) {
            RowStore row = values.row(0);
            this.values = row;

            // replace string status with actual MessageState
            MessageState status = MessageState.fromCode(row.get(DatabaseConstants.MSG_STATUS));
            this.values.replace(DatabaseConstants.MSG_STATUS, status);
        }
    }

    /**
     * Check if DLR params were actually found in the database
     * @return  true if found, false if not, false if otherwise invalid
     */
    public boolean existsInDb() {
        return this.values == null || this.values.isEmpty();
    }

    /**
     * Generate or get match map based on message ID, for a DbManager
     * @return  map of strings to match
     */
    private Map<String, ?> getMatch() {
        if (this.match == null) {
            Map<String, String> match = new ConcurrentHashMap<>();
            match.put(DatabaseConstants.MSG_ID, this.messageId);

            this.match = match;
        }

        return this.match;
    }

    /**
     * Validate a new status and persist it to the database
     * @param status    valid {@link MessageState}
     */
    public void persistNewStatus(MessageState status) {
        final Database database = Context.getDatabase();
        this.values.replace(DatabaseConstants.MSG_STATUS, status);

        if (database == null)
            return;

        // when we insert the new value into the database, it must be as a string
        Map<String, Object> tmpMap = new ConcurrentHashMap<>(this.values.toMap());
        tmpMap.replace(DatabaseConstants.MSG_STATUS, status.toString());

        database.update(DatabaseConstants.DLR_TABLE, tmpMap, getMatch());
    }

    /**
     * Get {@link DaliaSessionBridge bridge} for the fetched SMPP user
     * @return  bridge object, or null if no data was found in database
     */
    public DaliaSessionBridge getBridge() {
        if (this.values == null)
            return null;

        return DaliaSmppSessionListener.getSessionBridge(this.values.get(DatabaseConstants.SMPP_USER));
    }

    /**
     * Check if client actually wants the DLR update that we have received
     * @return  true if it is wanted, otherwise false
     */
    public boolean clientWantsUpdate() {
        MessageState status = this.values.get(DatabaseConstants.MSG_STATUS);

        boolean failureOnly  = this.values.get(DatabaseConstants.FAILURE_ONLY);
        boolean intermediate = this.values.get(DatabaseConstants.INTERMEDIATE);

        boolean statusIsFinal = MessageState.isFinal(status);
        return (!failureOnly && statusIsFinal)
                || (intermediate && !statusIsFinal)
                || (failureOnly && MessageState.isError(status));
    }

    /**
     * Convert request into a {@link DeliveryReport} object
     * @return  equivalent DeliveryReport, or null if no data found in database
     */
    public DeliveryReport toDeliveryReport() {
        if (this.values == null)
            return null;

        MessageState status = this.values.get(DatabaseConstants.MSG_STATUS);

        DeliveryReport deliveryReport = new DeliveryReport();
        deliveryReport.setId(this.values.get(DatabaseConstants.MSG_ID));
        deliveryReport.setSubmitDate(this.values.get(DatabaseConstants.SUBMIT_DATE));
        deliveryReport.setSourceAddr(this.values.get(DatabaseConstants.SOURCE_ADDR));
        deliveryReport.setDestAddr(this.values.get(DatabaseConstants.DEST_ADDR));
        deliveryReport.setIsIntermediate(!MessageState.isFinal(status));
        deliveryReport.setStatus(status);

        return deliveryReport;
    }
}
