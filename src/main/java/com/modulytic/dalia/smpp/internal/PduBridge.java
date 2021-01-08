package com.modulytic.dalia.smpp.internal;

import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.SmppRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class PduBridge<T extends SmppRequest> {
    private final T pdu;

    private boolean sourceAddressSet;
    private AppAddress sourceAddress;

    private boolean destAddressSet;
    private AppAddress destAddress;

    private boolean registeredDeliverySet;
    private RegisteredDelivery registeredDelivery;

    private boolean destAddressesSet;
    private final List<AppAddress> destAddresses = new ArrayList<>();

    public PduBridge(T pdu) {
        this.pdu = pdu;
    }

    public T getPdu() {
        return pdu;
    }

    /**
     * Call a getter on a PDU object and create a new object of type S with result as a constructor argument
     * @param pdu       PDU to get field from
     * @param methodName    method to call on PDU
     * @param to        class of object to instantiate with returned value
     * @param <S>       type of to
     * @return          new object, or null if an error occurred
     */
    private static <S> S convertField(SmppRequest pdu, String methodName, Class<S> to) {
        try {
            Class<? extends SmppRequest> pduClass = pdu.getClass();

            Method pduMethod = pduClass.getMethod(methodName);
            Constructor<S> constructor = to.getConstructor(pduMethod.getReturnType());

            return constructor.newInstance(pduMethod.invoke(pdu));
        }
        catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | InstantiationException ignored) {}

        return null;
    }

    public AppAddress getSourceAddress() {
        if (!sourceAddressSet) {
            sourceAddress = convertField(pdu, "getSourceAddress", AppAddress.class);
            sourceAddressSet = true;
        }

        return sourceAddress;
    }

    public AppAddress getDestAddress() {
        if (!destAddressSet) {
            destAddress = convertField(pdu, "getDestAddress", AppAddress.class);
            destAddressSet = true;
        }

        return destAddress;
    }

    @SuppressWarnings({"unchecked", "PMD.AvoidInstantiatingObjectsInLoops"})
    public List<AppAddress> getDestAddresses() {
        if (!destAddressesSet) {
            List<Address> gescobarAddresses = convertField(pdu, "getDestAddresses", List.class);

            if (gescobarAddresses != null) {
                for (Address address : gescobarAddresses) {
                    destAddresses.add(new AppAddress(address));
                }
            }

            destAddressesSet = true;
        }

        return destAddresses;
    }

    public RegisteredDelivery getRegisteredDelivery() {
        if (!registeredDeliverySet) {
            registeredDelivery = convertField(pdu, "getRegisteredDelivery", RegisteredDelivery.class);
            registeredDeliverySet = true;
        }

        return registeredDelivery;
    }
}
