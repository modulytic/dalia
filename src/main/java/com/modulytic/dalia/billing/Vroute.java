package com.modulytic.dalia.billing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@SuppressWarnings("unused")
public class Vroute {
    private final int id;
    private final String name;
    private final float rate;
    private final int countryCode;

    public Vroute(Map<String, ?> vals) {
        this((Integer) vals.get("id"), (String) vals.get("vroute_name"), (Float) vals.get("rate"), (Integer) vals.get("country_code"));
    }

    public Vroute(int id, String name, float rate, int countryCode) {
        this.id = id;
        this.name = name;
        this.rate = rate;
        this.countryCode = countryCode;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
       return this.name;
    }

    public float getRate() {
        return this.rate;
    }

    public int getCountryCode() {
        return this.countryCode;
    }
}
