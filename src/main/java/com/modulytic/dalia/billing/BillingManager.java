package com.modulytic.dalia.billing;

import com.modulytic.dalia.local.include.DbManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Deal with saving and fetching billing data
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class BillingManager {
    private final DbManager database;
    public BillingManager(DbManager database) {
        this.database = database;
    }

    // get currently active vRoute for a given country code
    public Vroute getActiveVroute(int countryCode) {
        Map<String, Object> match = new TreeMap<>();
        match.put("country_code", countryCode);
        match.put("is_active", true);

        List<HashMap<String, ?>> rs = this.database.fetch("billing_vroutes", match);
        if (rs == null)         // empty
            return null;

        return new Vroute(rs.get(0));
    }

    public void logMessage(String messageId, String smppUser, int countryCode, Vroute vroute) {
        Map<String, Object> values = new TreeMap<>();
        values.put("msg_id", messageId);
        values.put("smpp_user", smppUser);
        values.put("country_code", countryCode);

        if (vroute != null) {
            values.put("vroute", vroute.getId());
            values.put("rate", vroute.getRate());
        }

        this.database.insert("billing_logs", values);
    }
}
