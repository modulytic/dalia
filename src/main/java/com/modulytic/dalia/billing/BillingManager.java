package com.modulytic.dalia.billing;

import com.google.common.collect.Table;
import com.modulytic.dalia.DaliaContext;

import java.util.LinkedHashMap;

/**
 * Deal with saving and fetching billing data
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class BillingManager {
    // get currently active vRoute for a given country code
    public Vroute getActiveVroute(int countryCode) {
        if (DaliaContext.getDatabase() == null)
            return null;

        LinkedHashMap<String, Object> match = new LinkedHashMap<>();
        match.put("country_code", countryCode);
        match.put("is_active", true);

        Table<Integer, String, Object> rs = DaliaContext.getDatabase().fetch("billing_vroutes", match);
        if (rs == null || rs.isEmpty())         // empty
            return null;

        return new Vroute(rs.row(0));
    }

    public void logMessage(String messageId, String smppUser, int countryCode, Vroute vroute) {
        if (DaliaContext.getDatabase() == null)
            return;

        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("msg_id", messageId);
        values.put("smpp_user", smppUser);
        values.put("country_code", countryCode);

        if (vroute != null) {
            values.put("vroute", vroute.getId());
            values.put("rate", vroute.getRate());
        }

        DaliaContext.getDatabase().insert("billing_logs", values);
    }
}
