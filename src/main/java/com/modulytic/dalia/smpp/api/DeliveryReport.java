package com.modulytic.dalia.smpp.api;

import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.DeliverSm;

import java.sql.Timestamp;

public class DeliveryReport {
    private final DeliverSm deliverSm;

    private String id;
    private int submitDate;
    private final int doneDate;
    private String status;
    private int error = 0;

    public DeliveryReport() {
        this.deliverSm = new DeliverSm();

        this.doneDate = (int)(System.currentTimeMillis() / 1000);

        // set bit 2 of esm_class to mark as delivery report
        setEsmClassField(true, 2);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSubmitDate(Timestamp submitDate) {
        this.submitDate = (int)(submitDate.getTime() / 1000);
    }

    public void setSourceAddr(String src) {
        this.deliverSm.setSourceAddress(new Address().withAddress(src));
    }

    public void setDestAddr(String dst) {
        this.deliverSm.setDestAddress(new Address().withAddress(dst));
    }

    public void setIsIntermediate(boolean intermediate) {
        // set bit 5 of esm_class if true to mark as intermediate
        setEsmClassField(intermediate, 5);
    }

    private void setEsmClassField(boolean value, int pos) {
        byte esmClass = this.deliverSm.getEsmClass();

        if (value) {
            esmClass |= 1 << pos;
        }
        else {
            esmClass &= ~(1 << pos);
        }

        this.deliverSm.setEsmClass(esmClass);
    }

    public void setError(int error) {
        this.error = error;
    }

    public void setStatus(String status) {
        if (MessageState.isValid(status))
            this.status = status;
    }

    public DeliverSm toDeliverSm() {
        String sm = String.format("id:%s submit date:%d done date:%d stat:%s err:%d",
                                        this.id,
                                        this.submitDate,
                                        this.doneDate,
                                        this.status,
                                        this.error);
        this.deliverSm.setShortMessage(sm.getBytes());

        return this.deliverSm;
    }
}
