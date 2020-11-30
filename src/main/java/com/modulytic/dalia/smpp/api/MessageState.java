package com.modulytic.dalia.smpp.api;

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

    public static boolean isFinal(String status) {
        return !(status.equals(MessageState.DELIVERED)
                    || status.equals(MessageState.EN_ROUTE)
                    || status.equals(MessageState.UNKNOWN));
    }

    public static boolean isError(String status) {
        return (status.equals(MessageState.UNDELIVERABLE)
                    || status.equals(MessageState.REJECTED)
                    || status.equals(MessageState.EXPIRED));
    }

    public static boolean isValid(String status) {
        if (status.length() != MessageState.ACCEPTED.length())      // all statuses are the same length
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
