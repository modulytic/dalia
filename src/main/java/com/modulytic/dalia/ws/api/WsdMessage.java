package com.modulytic.dalia.ws.api;

import com.google.gson.Gson;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class WsdMessage {
    private final String name;
    private final String id;
    private final Map<String, ?> params;

    public WsdMessage(String name, Map<String, ?> params) {
        this.name = name;
        this.params = params;
        this.id = UUID.randomUUID().toString();
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public Map<String, ?> getParams() {
        return this.params;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
