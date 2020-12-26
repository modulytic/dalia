package com.modulytic.dalia.ws.api;

import com.google.gson.Gson;

import java.util.Map;
import java.util.UUID;

/**
 * Ws-daemon message, handled by Gson
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
@SuppressWarnings("unused")
public class WsdMessage {
    /**
     * name field, specifies action to take
     */
    private final String name;

    /**
     * ID of ws-daemon message, NOT SMS!
     */
    private final String id;

    /**
     * Params passed to command
     */
    private final Map<String, ?> params;

    /**
     * Create new message with given name and params, and randomly-generated ID
     * @param name      name of script to trigger on endpoint
     * @param params    params to pass to endpoint
     */
    public WsdMessage(String name, Map<String, ?> params) {
        this.name = name;
        this.params = params;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * get name field
     * @return  name in string format
     */
    public String getName() {
        return this.name;
    }

    /**
     * get ID of message
     * @return  ID, string
     */
    public String getId() {
        return this.id;
    }

    /**
     * get params field
     * @return  map of params
     */
    public Map<String, ?> getParams() {
        return this.params;
    }

    /**
     * Convert message to string for sending over WebSockets
     * @return  string in JSON format
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
