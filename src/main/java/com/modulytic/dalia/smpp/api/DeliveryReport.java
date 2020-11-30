package com.modulytic.dalia.smpp.api;

import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.DeliverSm;

import java.sql.Timestamp;

/**
 * Class to build DLR PDU easily from raw Java data
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
@SuppressWarnings("unused")
public class DeliveryReport {
    /**
     * deliver_sm PDU that can be sent
     */
    private final DeliverSm deliverSm;

    /**
     * ID of message we are updating status of
     */
    private String id;

    /**
     * Date originally submitted
     */
    private int submitDate;

    /**
     * Date final status was updated
     */
    private final int doneDate;

    /**
     * New status being sent
     */
    private String status;

    /**
     * Error code, if any encountered
     */
    private int error = 0;

    public DeliveryReport() {
        this.deliverSm = new DeliverSm();

        this.doneDate = (int)(System.currentTimeMillis() / 1000);

        // set bit 2 of esm_class to mark as delivery report
        setEsmClassField(true, 2);
    }

    /**
     * Set ID of deliver_sm
     * @param id    ID returned from submit_sm
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Use SQL Timestamp to set the submit date of the original message
     * @param submitDate    Timestamp of when message was submitted
     */
    public void setSubmitDate(Timestamp submitDate) {
        // TODO fix this! it's not a Unix timestamp, see https://smpp.org/smpp-delivery-receipt.html
        this.submitDate = (int)(submitDate.getTime() / 1000);
    }

    /**
     * Set source address of DLR
     * @param src   String, directly from submit_sm's destination address
     */
    public void setSourceAddr(String src) {
        this.deliverSm.setSourceAddress(new Address().withAddress(src));
    }

    /**
     * Set destination address of DLR
     * @param dst   String, directly from submit_sm's source address
     */
    public void setDestAddr(String dst) {
        this.deliverSm.setDestAddress(new Address().withAddress(dst));
    }

    /**
     * Set whether DLR is an intermediate notification, or whether it is informing of a final status
     * @param intermediate  boolean: true if intermediate, or false if final
     */
    public void setIsIntermediate(boolean intermediate) {
        // set bit 5 of esm_class if true to mark as intermediate
        setEsmClassField(intermediate, 5);
    }

    /**
     * Set bits in the DLR's esm_class
     * @param value whether to set the bit to true or false
     * @param pos   which bit's value to sit
     */
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

    /**
     * Set error code of DLR (default 0)
     * @param error integer SMPP error code
     */
    public void setError(int error) {
        this.error = error;
    }

    /**
     * Set status of DLR
     * @param status    valid {@link MessageState}
     */
    public void setStatus(String status) {
        if (MessageState.isValid(status))
            this.status = status;
    }

    /**
     * Convert our DeliveryReport to an actual PDU that can be sent
     * @return  {@link DeliverSm} PDU
     */
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
