package com.modulytic.dalia.smpp.api;

/**
 * Java interface to SMPP registered_delivery (see page 124 of the <a href="https://smpp.org/SMPP_v3_4_Issue1_2.pdf">SMPP 3.4 spec</a>)
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class RegisteredDelivery {
    /**
     * Whether or not endpoint should forward DLRs along
     */
    private final boolean forwardDlrs;

    /**
     * Whether client wants all final DLRs
     */
    private final boolean receiveFinal;

    /**
     * Whether client only wants final failure DLRs
     */
    private boolean failureOnly;

    /**
     * Whether client wants intermediate DLRs
     */
    private final boolean intermediate;

    /**
     * Parse RegisteredDelivery fields (as a byte) and convert to class with booleans for easy testing
     * @param registeredDelivery    valid SMPP registered_delivery
     */
    public RegisteredDelivery(byte registeredDelivery) {
        this.receiveFinal = checkBit(registeredDelivery, 0);
        this.failureOnly  = checkBit(registeredDelivery, 1);
        this.intermediate = checkBit(registeredDelivery, 4);

        // if both fields are set, ignore failure only
        if (this.receiveFinal)
            this.failureOnly = false;

        this.forwardDlrs = (receiveFinal || failureOnly || intermediate);
    }

    /**
     * Get whether endpoint should forward delivery reports or not
     * @return  true if should forward, otherwise false
     */
    public boolean getForwardDlrs() {
        return this.forwardDlrs;
    }

    /**
     * Get whether client wants final delivery reports
     * @return  true if should receive final, otherwise false
     */
    public boolean getReceiveFinal() {
        return this.receiveFinal;
    }

    /**
     * Get whether client only wants final delivery reports
     * @return  true if only wants failure, otherwise false
     */
    public boolean getFailureOnly() {
        return this.failureOnly;
    }

    /**
     * Get whether client wants intermediate status updates
     * @return  true if wants intermediate, otherwise false
     */
    public boolean getIntermediate() {
        return this.intermediate;
    }

    /**
     * Check bit of byte
     * @param b     byte to check
     * @param pos   number of field
     * @return      true if field is set, or false if not
     */
    private static boolean checkBit(byte b, int pos) {
        return (b << ~pos < 0);
    }
}
