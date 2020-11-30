package com.modulytic.dalia.billing;

import java.util.Map;

/**
 * Represent a vRoute (virtual route) that exists for billing in Java
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
@SuppressWarnings("unused")
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
    public Vroute(Map<String, ?> rs) {
        this((Integer) rs.get("id"), (String) rs.get("vroute_name"), (Float) rs.get("rate"), (Integer) rs.get("country_code"));
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
