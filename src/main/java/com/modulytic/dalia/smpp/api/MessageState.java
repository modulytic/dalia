package com.modulytic.dalia.smpp.api;


import com.modulytic.dalia.include.BiHashMap;

/**
 * Possible states for a message, used in DLRs or Query_SMs
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public enum MessageState {
    ACCEPTED,
    UNDELIVERABLE,
    REJECTED,
    DELIVERED,
    EXPIRED,
    DELETED,
    EN_ROUTE,
    UNKNOWN;

    private BiHashMap<MessageState, String> codeMappings = new BiHashMap<>();

    /**
     * Check if message status is final. Does NOT check if status is valid!
     * @param status    a MessageState
     * @return          true if valid, false otherwise
     */
    public static boolean isFinal(MessageState status) {
        return !(status.equals(ACCEPTED)
                    || status.equals(EN_ROUTE)
                    || status.equals(UNKNOWN));
    }

    /**
     * Check if message status indicates that there was an error. Does NOT check if status is valid!
     * @param status    a MessageState
     * @return          true if error, false otherwise
     */
    public static boolean isError(MessageState status) {
        return (status.equals(UNDELIVERABLE)
                    || status.equals(REJECTED)
                    || status.equals(EXPIRED));
    }

    private static BiHashMap<MessageState, String> getCodeMappings() {
        BiHashMap<MessageState, String> codeMappings = new BiHashMap<>();
        codeMappings.put(ACCEPTED,      "ACCEPTD");
        codeMappings.put(UNDELIVERABLE, "UNDELIV");
        codeMappings.put(REJECTED,      "REJECTD");
        codeMappings.put(DELIVERED,     "DELIVRD");
        codeMappings.put(EXPIRED,       "EXPIRED");
        codeMappings.put(DELETED,       "DELETED");
        codeMappings.put(EN_ROUTE,      "ENROUTE");
        codeMappings.put(UNKNOWN,       "UNKNOWN");

        return codeMappings;
    }

    public static MessageState fromCode(String code) {
        if (code == null)
            return null;

        return getCodeMappings().getBackward(code);
    }

    @Override
    public String toString() {
        if (codeMappings.isEmpty())
            codeMappings = getCodeMappings();

        return codeMappings.getForward(this);
    }
}
