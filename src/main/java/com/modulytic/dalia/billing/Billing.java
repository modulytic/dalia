package com.modulytic.dalia.billing;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.app.database.include.DatabaseConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deal with saving and fetching billing data
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class Billing {
    private Billing() {}

    public static void logMessage(String messageId, String smppUser, int countryCode, Vroute vroute) {
        Database database = Context.getDatabase();
        if (database == null)
            return;

        Map<String, Object> values = new ConcurrentHashMap<>();
        values.put(DatabaseConstants.MSG_ID, messageId);
        values.put(DatabaseConstants.SMPP_USER, smppUser);
        values.put(DatabaseConstants.COUNTRY_CODE, countryCode);

        if (vroute != null) {
            values.put(DatabaseConstants.VROUTE, vroute.getId());
            values.put(DatabaseConstants.RATE, vroute.getRate());
        }

        database.insert(DatabaseConstants.LOG_TABLE, values);
    }
}
