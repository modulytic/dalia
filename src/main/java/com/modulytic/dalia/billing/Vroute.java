package com.modulytic.dalia.billing;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.DatabaseResults;
import com.modulytic.dalia.app.database.RowStore;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.app.database.include.DatabaseConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represent a vRoute (virtual route) that exists for billing in Java
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class Vroute {
    private final int id;
    private final String name;
    private final float rate;
    private final int countryCode;

    /**
     * Create a new vRoute object from DbManager data
     * <p>
     *     Columns should be: int id, String vroute_name, float rate, int country_code
     * </p>
     * @param rs    data returned from a DbManager fetch request
     */
    public Vroute(RowStore rs) {
        this(rs.get(DatabaseConstants.VROUTE_ID),
                rs.get(DatabaseConstants.VROUTE_NAME),
                rs.get(DatabaseConstants.RATE),
                rs.get(DatabaseConstants.COUNTRY_CODE));
    }

    /**
     * Create a new vRoute object from Java data
     * @param id            ID of vRoute
     * @param name          Human-readable name of vRoute
     * @param rate          Billing rate for this vRoute
     * @param countryCode   Calling code for this vRoute
     */
    public Vroute(int id, String name, float rate, int countryCode) {
        this.id = id;
        this.name = name;
        this.rate = rate;
        this.countryCode = countryCode;
    }

    // get currently active vRoute for a given country code
    public static Vroute getActiveVroute(int countryCode) {
        Database database = Context.getDatabase();
        if (database == null)
            return null;

        Map<String, Object> match = new ConcurrentHashMap<>();
        match.put(DatabaseConstants.COUNTRY_CODE, countryCode);
        match.put(DatabaseConstants.IS_ACTIVE, true);

        DatabaseResults rs = database.fetch(DatabaseConstants.VROUTE_TABLE, match);
        if (rs == null || rs.isEmpty())
            return null;

        return new Vroute(rs.row(0));
    }

    /**
     * Get ID of vRoute
     * @return  integer ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get name of vRoute
     * @return  string name
     */
    public String getName() {
       return this.name;
    }

    /**
     * Get billing rate of vRoute
     * @return  float rate
     */
    public float getRate() {
        return this.rate;
    }

    /**
     * Get country code of vRoute
     * @return  int country code
     */
    public int getCountryCode() {
        return this.countryCode;
    }
}
