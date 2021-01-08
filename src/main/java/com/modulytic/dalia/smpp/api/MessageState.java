package com.modulytic.dalia.smpp.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Possible states for a message, used in DLRs or Query_SMs
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public enum MessageState {
    ENROUTE(1),
    DELIVERED(2),
    EXPIRED(3),
    DELETED(4),
    UNDELIVERABLE(5),
    ACCEPTED(6),
    UNKNOWN(7),
    REJECTED(8);

    private static final Map<String, MessageState> values = new ConcurrentHashMap<>();
    private static final String[] names = {
        "ENROUTE",
        "DELIVRD",
        "EXPIRED",
        "DELETED",
        "UNDELIV",
        "ACCEPTD",
        "UNKNOWN",
        "REJECTD"
    };

    private final byte state;
    MessageState(int state) {
        this.state = (byte) state;
    }

    /**
     * Check if message status is final. Does NOT check if status is valid!
     * @param status    a MessageState
     * @return          true if valid, false otherwise
     */
    public static boolean isFinal(MessageState status) {
        return !(status.equals(ACCEPTED)
                    || status.equals(ENROUTE)
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
        return names[this.state - 1];
    }

    public byte toSmpp() {
        return state;
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
