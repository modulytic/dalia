package com.modulytic.dalia.smpp.api;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import net.gescobar.smppserver.packet.Address;

/**
 * {@link Address} that adds country codes and formatting from libphonenumber, and checks validity
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class SMSCAddress extends Address {
    /**
     * Instance of libphonenumber, can format and parse numbers
     */
    private PhoneNumberUtil phoneUtil = null;

    /**
     * Result of parsing phone number with {@link this#phoneUtil}
     */
    private PhoneNumber phoneNumber = null;

    /**
     * Whether or not the SMSC supports this format of number
     */
    private boolean isSupported;

    public SMSCAddress(Address address) {
        super();

        super.setAddress(address.getAddress());
        super.setNpi(address.getNpi());
        super.setTon(address.getTon());

        // if this number's TON or NPI is not supported, do not mark it as supported
        this.isSupported = (isValidTon() && isValidNpi());
        if (!isSupported())
            return;

        // set up our number parser and formatter
        phoneUtil = PhoneNumberUtil.getInstance();

        // parse phone number and mark as invalid if there are any issues
        try {
            this.phoneNumber = phoneUtil.parse(getAddress(), "US");
        }
        catch (NumberParseException e) {
            this.isSupported = false;
        }
    }

    /**
     * If the SMSC supports our TON
     * @return  true if it is supported, otherwise false
     */
    public boolean isValidTon() {
        return (getTon() == TON.INTERNATIONAL || getTon() == TON.NATIONAL);
    }

    /**
     * If the SMSC supports our NPI
     * @return  true if it is supported, otherwise false
     */
    public boolean isValidNpi() {
        return (getNpi() == NPI.E164 || getNpi() == NPI.NATIONAL);
    }

    /**
     * If the SMSC supported this address's format
     * @return  true if it is supported, otherwise false
     */
    public boolean isSupported() {
        return this.isSupported;
    }

    /**
     * Get the address's country code
     * @return  country code if supported, null if not
     */
    public Integer getCountryCode() {
        if (!isSupported())
            return null;

        return this.phoneNumber.getCountryCode();
    }

    /**
     * Get the address in E164 format, so we know it is not misread by other components
     * @return  phone string in E164 format if supported, null if not
     */
    public String toE164() {
        if (!isSupported())
            return null;

        return this.phoneUtil.format(this.phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }
}
