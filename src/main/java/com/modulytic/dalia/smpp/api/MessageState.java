package com.modulytic.dalia.smpp.api;

/**
 * Possible states for a message, used in DLRs or Query_SMs
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
@SuppressWarnings("unused")
public class MessageState {
    public static String ACCEPTED      = "ACCEPTD";
    public static String UNDELIVERABLE = "UNDELIV";
    public static String REJECTED      = "REJECTD";
    public static String DELIVERED     = "DELIVRD";
    public static String EXPIRED       = "EXPIRED";
    public static String DELETED       = "DELETED";
    public static String EN_ROUTE      = "ENROUTE";
    public static String UNKNOWN       = "UNKNOWN";

    /**
     * Check if message status is final. Does NOT check if status is valid!
     * @param status    a MessageState
     * @return          true if valid, false otherwise
     */
    public static boolean isFinal(String status) {
        return !(status.equals(MessageState.ACCEPTED)
                    || status.equals(MessageState.EN_ROUTE)
                    || status.equals(MessageState.UNKNOWN));
    }

    /**
     * Check if message status indicates that there was an error. Does NOT check if status is valid!
     * @param status    a MessageState
     * @return          true if error, false otherwise
     */
    public static boolean isError(String status) {
        return (status.equals(MessageState.UNDELIVERABLE)
                    || status.equals(MessageState.REJECTED)
                    || status.equals(MessageState.EXPIRED));
    }

    /**
     * Check if status is valid
     * @param status    any string
     * @return          true if it is a valid MessageState, otherwise false
     */
    public static boolean isValid(String status) {
        if (status == null)
            return false;

        // all statuses are the same length, so it might save us a lot of comparisons
        if (status.length() != MessageState.ACCEPTED.length())
            return false;

        return (status.equals(MessageState.DELIVERED)
                    || status.equals(MessageState.EN_ROUTE)
                    || status.equals(MessageState.UNKNOWN)
                    || status.equals(MessageState.UNDELIVERABLE)
                    || status.equals(MessageState.REJECTED)
                    || status.equals(MessageState.EXPIRED)
                    || status.equals(MessageState.ACCEPTED)
                    || status.equals(MessageState.DELETED));
    }
}
