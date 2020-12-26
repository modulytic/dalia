package com.modulytic.dalia.smpp.request;

import com.google.common.collect.Table;
import com.modulytic.dalia.DaliaContext;
import com.modulytic.dalia.local.include.DbConstants;
import com.modulytic.dalia.smpp.DaliaSessionBridge;
import com.modulytic.dalia.smpp.api.DeliveryReport;
import com.modulytic.dalia.smpp.api.MessageState;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
    private Map<String, Object> values;

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

        Table<Integer, String, Object> values = DaliaContext.getDatabase().fetch(DbConstants.DLR_TABLE, getMatch());
        if (!values.isEmpty()) {
            Map<String, Object> valuesMap = values.row(0);
            for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
                if (entry.getValue() instanceof String) {
                    MessageState status = MessageState.fromCode((String) entry.getValue());

                    if (status != null)
                        valuesMap.replace(entry.getKey(), status);
                }
            }

            setValues(valuesMap);
        }
    }

    /**
     * Populate values if they are valid
     * @param values    map of values fetched from database
     */
    private void setValues(Map<String, Object> values) {
        Map<String, Class<?>> requiredKeys = DaliaContext.getDatabase().getRequiredKeys();

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
     * Generate or get match map based on message ID, for a DbManager
     * @return  map of strings to match
     */
    private Map<String, ?> getMatch() {
        if (this.match == null) {
            Map<String, String> match = new ConcurrentHashMap<>();
            match.put(DbConstants.MSG_ID, this.messageId);

            this.match = match;
        }

        return this.match;
    }

    /**
     * Validate a new status and persist it to the database
     * @param status    valid {@link MessageState}
     */
    public void persistNewStatus(MessageState status) {
        this.values.replace(DbConstants.MSG_STATUS, status);

        if (DaliaContext.getDatabase() == null)
            return;

        Map<String, Object> tmpMap = (Map<String, Object>) ((LinkedHashMap<String, Object>) this.values).clone();
        tmpMap.replace(DbConstants.MSG_STATUS, status.toString());

        DaliaContext.getDatabase().update(DbConstants.DLR_TABLE, tmpMap, getMatch());
    }

    /**
     * Get {@link DaliaSessionBridge bridge} for the fetched SMPP user
     * @return  bridge object, or null if no data was found in database
     */
    public DaliaSessionBridge getBridge() {
        if (this.values == null)
            return null;

        return DaliaContext.getSessionListener().getSessionBridge((String) this.values.get(DbConstants.SMPP_USER));
    }

    /**
     * Check if client actually wants the DLR update that we have received
     * @return  true if it is wanted, otherwise false
     */
    public boolean clientWantsUpdate() {
        MessageState status = (MessageState) this.values.get(DbConstants.MSG_STATUS);

        boolean failureOnly  = (Boolean) this.values.get(DbConstants.FAILURE_ONLY);
        boolean intermediate = (Boolean) this.values.get(DbConstants.INTERMEDIATE);

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

        MessageState status = (MessageState) this.values.get(DbConstants.MSG_STATUS);

        DeliveryReport deliveryReport = new DeliveryReport();
        deliveryReport.setId((String) this.values.get(DbConstants.MSG_ID));
        deliveryReport.setSubmitDate((LocalDateTime) this.values.get(DbConstants.SUBMIT_DATE));
        deliveryReport.setSourceAddr((String) this.values.get(DbConstants.SOURCE_ADDR));
        deliveryReport.setDestAddr((String) this.values.get(DbConstants.DEST_ADDR));
        deliveryReport.setIsIntermediate(!MessageState.isFinal(status));
        deliveryReport.setStatus(status);

        return deliveryReport;
    }
}
