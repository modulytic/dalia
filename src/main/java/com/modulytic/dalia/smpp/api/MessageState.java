package com.modulytic.dalia.smpp.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Possible states for a message, used in DLRs or Query_SMs
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public enum MessageState {
    ACCEPTED("ACCEPTD"),
    UNDELIVERABLE("UNDELIV"),
    REJECTED("REJECTD"),
    DELIVERED("DELIVRD"),
    EXPIRED("EXPIRED"),
    DELETED("DELETED"),
    EN_ROUTE("ENROUTE"),
    UNKNOWN("UNKNOWN");

    private static final Map<String, MessageState> values = new ConcurrentHashMap<>();

    final private String state;
    MessageState(String state) {
        this.state = state;
    }

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

    @Override
    public String toString() {
        return this.state;
    }

    public static MessageState fromCode(String code) {
        if (code == null)
            return null;

        if (values.isEmpty()) {
            for (MessageState state : MessageState.values())
                values.put(state.toString(), state);
        }

        return values.get(code);
    }
}
