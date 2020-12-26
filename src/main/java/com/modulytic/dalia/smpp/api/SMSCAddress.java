package com.modulytic.dalia.smpp.api;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.modulytic.dalia.Constants;
import net.gescobar.smppserver.packet.Address;

/**
 * {@link Address} that adds country codes and formatting from libphonenumber, and checks validity
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class SMSCAddress extends Address {
    /**
     * Instance of libphonenumber, can format and parse numbers
     */
    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    /**
     * Result of parsing phone number with {@link this#phoneUtil}
     */
    private PhoneNumber phoneNumber;

    /**
     * Whether or not the SMSC supports this format of number
     */
    private boolean supported;

    public SMSCAddress(Address address) {
        super();

        super.setAddress(address.getAddress());
        super.setNpi(address.getNpi());
        super.setTon(address.getTon());

        // if this number's TON or NPI is not supported, do not mark it as supported
        this.supported = (isValidTon(address.getTon()) && isValidNpi(address.getNpi()));
        if (!this.supported)
            return;

        // parse phone number and mark as invalid if there are any issues
        try {
            this.phoneNumber = phoneUtil.parse(getAddress(), Constants.DEFAULT_ADDRESS_REGION);
        }
        catch (NumberParseException e) {
            this.supported = false;
        }
    }

    /**
     * If the SMSC supports our TON
     * @return  true if it is supported, otherwise false
     */
    public boolean isValidTon() {
        return isValidTon(getTon());
    }

    /**
     * If the SMSC supports our NPI
     * @return  true if it is supported, otherwise false
     */
    public boolean isValidNpi() {
        return isValidNpi(getNpi());
    }

    private static boolean isValidTon(byte ton) {
        return (ton == TON.INTERNATIONAL || ton == TON.NATIONAL);
    }

    private static boolean isValidNpi(byte npi) {
        return (npi == NPI.E164 || npi == NPI.NATIONAL);
    }

    /**
     * If the SMSC supported this address's format
     * @return  true if it is supported, otherwise false
     */
    public boolean getSupported() {
        return this.supported;
    }

    /**
     * Get the address's country code
     * @return  country code if supported, null if not
     */
    public Integer getCountryCode() {
        if (!getSupported())
            return null;

        return this.phoneNumber.getCountryCode();
    }

    /**
     * Get the address in E164 format, so we know it is not misread by other components
     * @return  phone string in E164 format if supported, null if not
     */
    public String toE164() {
        if (!getSupported())
            return null;

        return phoneUtil.format(this.phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }
}
