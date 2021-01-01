package com.modulytic.dalia.ws.api;

public final class WsdResponseCode {
    private WsdResponseCode() {}

    public static final int NO_CLIENTS           = -99;
    public static final int SUCCESS              = 0x0;
    public static final int DATABASE_ERROR       = 0x1;
    public static final int MISSING_PARAMETER    = 0x2;
    public static final int NO_AVAILABLE_SENDERS = 0x3;
    public static final int INTERNAL_SEND_ERROR  = 0x4;
    public static final int MSG_ALREADY_SENT     = 0x5;
    public static final int ID_NOT_FOUND         = 0x6;
}
