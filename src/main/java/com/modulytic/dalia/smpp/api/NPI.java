package com.modulytic.dalia.smpp.api;

/**
 * Possible values for SMPP <a href="https://docs.aerialink.net/api/smpp/ton-npi-settings/">NPI</a>
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
@SuppressWarnings("unused")
public class NPI {
    public static byte UNKNOWN = 0;
    public static byte E164 = 1;
    public static byte NATIONAL = 8;
    public static byte PRIVATE = 9;
}
