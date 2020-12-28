package com.modulytic.dalia.billing;

import com.google.common.collect.Table;
import com.modulytic.dalia.DaliaContext;
import com.modulytic.dalia.local.include.DbConstants;
import com.modulytic.dalia.local.include.DbManager;

import java.util.LinkedHashMap;

/**
 * Deal with saving and fetching billing data
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class BillingManager {

    // get currently active vRoute for a given country code
    public Vroute getActiveVroute(int countryCode) {
        DbManager database = DaliaContext.getDatabase();
        if (database == null)
            return null;

        LinkedHashMap<String, Object> match = new LinkedHashMap<>();
        match.put(DbConstants.COUNTRY_CODE, countryCode);
        match.put(DbConstants.IS_ACTIVE, true);

        Table<Integer, String, Object> rs = database.fetch(DbConstants.VROUTE_TABLE, match);
        if (rs == null || rs.isEmpty())         // empty
            return null;

        return new Vroute(rs.row(0));
    }

    public void logMessage(String messageId, String smppUser, int countryCode, Vroute vroute) {
        DbManager database = DaliaContext.getDatabase();
        if (database == null)
            return;

        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put(DbConstants.MSG_ID, messageId);
        values.put(DbConstants.SMPP_USER, smppUser);
        values.put(DbConstants.COUNTRY_CODE, countryCode);

        if (vroute != null) {
            values.put(DbConstants.VROUTE, vroute.getId());
            values.put(DbConstants.RATE, vroute.getRate());
        }

        database.insert(DbConstants.LOG_TABLE, values);
    }
}
