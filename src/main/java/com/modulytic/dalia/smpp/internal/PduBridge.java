package com.modulytic.dalia.smpp.internal;

import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.SmppRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PduBridge<T extends SmppRequest> {
    private final T pdu;

    private boolean sourceAddressSet;
    private AppAddress sourceAddress;

    private boolean destAddressSet;
    private AppAddress destAddress;

    private boolean registeredDeliverySet;
    private RegisteredDelivery registeredDelivery;

    public PduBridge(T pdu) {
        this.pdu = pdu;
    }

    public T getPdu() {
        return pdu;
    }

    /**
     * Call getter "method" on "pdu", expect class "from" (or primitive version), and pass to new "to" constructor
     * @param pdu       PDU to get field from
     * @param method    method to call on PDU
     * @param from      return class of method
     * @param to        class of object to instantiate with returned value
     * @param <S>       type of to
     * @param <U>       type of from
     * @return          new object, or null if an error occurred
     */
    private static <S, U> S convertField(SmppRequest pdu, String method, Class<U> from, Class<S> to) {
        try {
            Method m = pdu.getClass().getMethod(method);
            Class<?> returnType = m.getReturnType();

            // if the return type is primitive, assume from is the object version of that type
            // perform the first step so that we know we actually got the primitive we were expecting
            if (returnType.isPrimitive()) {
                U originalResult = from.getConstructor(returnType).newInstance(m.invoke(pdu));
                return to.getConstructor(returnType).newInstance(originalResult);
            }
            else if (from.isAssignableFrom(returnType)) {
                return to.getConstructor(from).newInstance(m.invoke(pdu));
            }
        }
        catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | InstantiationException ignored) {}

        return null;
    }

    public AppAddress getSourceAddress() {
        if (!sourceAddressSet) {
            sourceAddress = convertField(pdu, "getSourceAddress", Address.class, AppAddress.class);
            sourceAddressSet = true;
        }

        return sourceAddress;
    }

    public AppAddress getDestAddress() {
        if (!destAddressSet) {
            destAddress = convertField(pdu, "getDestAddress", Address.class, AppAddress.class);
            destAddressSet = true;
        }

        return destAddress;
    }

    public RegisteredDelivery getRegisteredDelivery() {
        if (!registeredDeliverySet) {
            registeredDelivery = convertField(pdu, "getRegisteredDelivery", Byte.class, RegisteredDelivery.class);
            registeredDeliverySet = true;
        }

        return registeredDelivery;
    }
}
