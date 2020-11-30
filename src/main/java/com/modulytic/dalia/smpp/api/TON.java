package com.modulytic.dalia.smpp.api;

// https://docs.aerialink.net/api/smpp/ton-npi-settings/
@SuppressWarnings("unused")
public class TON {
    public static byte UNKNOWN = 0;
    public static byte INTERNATIONAL = 1;
    public static byte NATIONAL = 2;
    public static byte NETWORK = 3;
    public static byte SUBSCRIBER = 4;
    public static byte ALPHANUMERIC = 5;
    public static byte ABBREVIATED = 6;
}
