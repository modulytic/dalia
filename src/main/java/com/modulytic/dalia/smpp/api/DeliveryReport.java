package com.modulytic.dalia.smpp.api;

import com.modulytic.dalia.smpp.internal.SmppTime;
import net.gescobar.smppserver.Response;
import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.DeliverSm;
import net.gescobar.smppserver.packet.Tlv;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Class to build DLR PDU easily from raw Java data
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
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
    private LocalDateTime submitDate;

    /**
     * Date final status was updated
     */
    private final LocalDateTime doneDate;

    /**
     * New status being sent
     */
    private MessageState status;

    /**
     * Error code, if any encountered. Default: 0
     */
    private byte error;

    public DeliveryReport() {
        this.deliverSm = new DeliverSm();

        this.doneDate = LocalDateTime.now();

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
     * @param dt    Timestamp of when message was submitted
     */
    public void setSubmitDate(LocalDateTime dt) {
        this.submitDate = dt;
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
        this.error = (byte) error;
    }

    /**
     * Set status of DLR
     * @param status    valid {@link MessageState}
     */
    public void setStatus(MessageState status) {
        this.status = status;
    }

    /**
     * Convert our DeliveryReport to an actual PDU that can be sent
     * @return  {@link DeliverSm} PDU
     */
    public DeliverSm toDeliverSm() {
        // Set the short message with all our data
        String sm = String.format("id:%s submit date:%s done date:%s stat:%s err:%d",
                                        this.id,
                                        SmppTime.formatAbsolute(this.submitDate),
                                        SmppTime.formatAbsolute(this.doneDate),
                                        this.status.toString(),
                                        this.error);
        this.deliverSm.setShortMessage(sm.getBytes());

        // Make sure the PDU's status is marked as OK
        this.deliverSm.setCommandStatus(Response.OK.getCommandStatus());

        // Now set it in optional parameters, too
        // encode message_state parameter
        Tlv state = new Tlv(Tlv.MESSAGE_STATE, new byte[]{this.status.toSmpp()}, "message_state");
        this.deliverSm.addOptionalParameter(state);

        // encode message ID in receipted_message_id
        Tlv id = new Tlv(Tlv.RECEIPTED_MESSAGE_ID, this.id.getBytes(StandardCharsets.ISO_8859_1), "receipted_message_id");
        this.deliverSm.addOptionalParameter(id);

        // encode network error code as network_error_code
        Tlv error = new Tlv(Tlv.NETWORK_ERROR_CODE, new byte[]{this.error}, "network_error_code");
        this.deliverSm.addOptionalParameter(error);

        return this.deliverSm;
    }
}
