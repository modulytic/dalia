package com.modulytic.dalia.smpp.api;

// https://smpp.org/SMPP_v3_4_Issue1_2.pdf
// pg. 124
public class RegisteredDelivery {
    private final boolean forwardDlrs;

    private final boolean receiveFinal;
    private final boolean failureOnly;
    private final boolean intermediate;

    public RegisteredDelivery(byte registeredDelivery) {
        this.receiveFinal = checkBit(registeredDelivery, 0);
        this.failureOnly  = checkBit(registeredDelivery, 1);
        this.intermediate = checkBit(registeredDelivery, 4);

        this.forwardDlrs = (receiveFinal || failureOnly || intermediate);
    }

    public boolean getForwardDlrs() {
        return this.forwardDlrs;
    }

    public boolean getReceiveFinal() {
        return this.receiveFinal;
    }

    public boolean getFailureOnly() {
        return this.failureOnly;
    }

    public boolean getIntermediate() {
        return this.intermediate;
    }

    private boolean checkBit(byte b, int pos) {
        return (b << ~pos < 0);
    }
}
