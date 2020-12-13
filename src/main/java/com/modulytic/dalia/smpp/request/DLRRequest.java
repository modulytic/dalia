package com.modulytic.dalia.smpp.request;

import com.google.common.collect.Table;
import com.modulytic.dalia.local.include.DbManager;
import com.modulytic.dalia.smpp.DaliaSessionBridge;
import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import com.modulytic.dalia.smpp.api.DeliveryReport;
import com.modulytic.dalia.smpp.api.InvalidStatusException;
import com.modulytic.dalia.smpp.api.MessageState;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

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
     * Name of SQL table
     */
    private final String dbTable = "dlr_status";

    /**
     * Active database interface
     */
    private DbManager database = null;

    /**
     * DLR params
     */
    private Map<String, Object> values = null;

    /**
     * Map of message ID, saved in case of repeated requests
     */
    private Map<String, String> match = null;

    /**
     * Build DLR params from database
     * @param messageId ID of message assigned when submitted
     * @param database  instance of some type of {@link DbManager}
     */
    public DLRRequest(String messageId, DbManager database) {
        setDatabase(database);
        this.messageId = messageId;

        Table<Integer, String, Object> values = this.database.fetch(this.dbTable, getMatch());
        if (!values.isEmpty())
            setValues(values.row(0));
    }

    /**
     * Populate values if they are valid
     * @param values    map of values fetched from database
     */
    private void setValues(Map<String, Object> values) {
        Map<String, Class<?>> requiredKeys = this.database.getRequiredKeys();

        for (String key : requiredKeys.keySet()) {
            if (!values.containsKey(key))
                return;

            Class<?> actualClass  = values.get(key).getClass();
            Class<?> correctClass = requiredKeys.get(key);
            if (!actualClass.isInstance(correctClass))
                return;
        }

        this.values = values;
    }

    /**
     * Check if DLR params were actually found in the database
     * @return  true if found, false if not, false if otherwise invalid
     */
    public boolean existsInDb() {
        return this.values == null || this.values.isEmpty();
    }

    /**
     * Set database connector
     * @param database  connector to set
     */
    public void setDatabase(DbManager database) {
        this.database = database;
    }

    /**
     * Generate or get match map based on message ID
     * @return  map of strings to match
     */
    private Map<String, String> getMatch() {
        if (this.match == null) {
            Map<String, String> match = new TreeMap<>();
            match.put("msg_id", this.messageId);

            this.match = match;
        }

        return this.match;
    }

    /**
     * Validate a new status and persist it to the database
     * @param status    valid {@link MessageState}
     * @throws InvalidStatusException   if status is not a valid {@link MessageState}
     */
    public void persistNewStatus(String status) throws InvalidStatusException {
        if (!MessageState.isValid(status))
            throw new InvalidStatusException(String.format("Invalid DLR status %s", status));

        if (this.database == null)
            return;

        this.values.replace("msg_status", status);
        this.database.update(this.dbTable, this.values, getMatch());
    }

    /**
     * Get {@link DaliaSessionBridge bridge} for the fetched SMPP user
     * @param listener  {@link DaliaSmppSessionListener}
     * @return  bridge object, or null if no data was found in database
     */
    public DaliaSessionBridge getBridge(DaliaSmppSessionListener listener) {
        if (this.values == null)
            return null;

        return listener.getSessionBridge((String) this.values.get("smpp_user"));
    }

    /**
     * Check if client actually wants the DLR update that we have received
     * @return  true if it is wanted, otherwise false
     */
    public boolean clientWantsUpdate() {
        String status = (String) this.values.get("msg_status");

        boolean failureOnly  = (Boolean) this.values.get("failure_only");
        boolean intermediate = (Boolean) this.values.get("intermediate");

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

        String status = (String) this.values.get("msg_status");

        DeliveryReport deliveryReport = new DeliveryReport();
        deliveryReport.setId((String) this.values.get("msg_id"));
        deliveryReport.setSubmitDate((Timestamp) this.values.get("submit_date"));
        deliveryReport.setSourceAddr((String) this.values.get("src_addr"));
        deliveryReport.setDestAddr((String) this.values.get("dst_addr"));
        deliveryReport.setIsIntermediate(!MessageState.isFinal(status));
        deliveryReport.setStatus(status);

        return deliveryReport;
    }
}
