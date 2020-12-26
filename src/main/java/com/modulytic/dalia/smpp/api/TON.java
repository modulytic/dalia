package com.modulytic.dalia.smpp.api;

/**
 * Possible values for SMPP <a href="https://docs.aerialink.net/api/smpp/ton-npi-settings/">TON</a>
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
@SuppressWarnings("unused")
public class TON {
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static byte UNKNOWN = 0;
    public static byte INTERNATIONAL = 1;
    public static byte NATIONAL = 2;
    public static byte NETWORK = 3;
    public static byte SUBSCRIBER = 4;
    public static byte ALPHANUMERIC = 5;
    public static byte ABBREVIATED = 6;
}
