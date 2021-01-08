package com.modulytic.dalia.ws.api;

final class WsdMessageConstants {
    private WsdMessageConstants() {}

    public static final String DESTINATION   = "dest_num";
    public static final String MESSAGE_ID    = "msg_id";
    public static final String FORWARD_DLR   = "dlr";
    public static final String VALID_FOR     = "validity_period";
    public static final String CONTENT       = "content";
    public static final String DELIVERY_TIME = "schedule_delivery_time";

    public static final class Scripts {
        private Scripts() {}

        public static final String SEND    = "send.php";
        public static final String REPLACE = "replace.php";
        public static final String CANCEL  = "cancel.php";
    }
}
