package com.modulytic.dalia.app.database;

import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.database.include.DatabaseConstants;
import com.modulytic.dalia.smpp.api.RegisteredDelivery;
import com.modulytic.dalia.smpp.internal.AppAddress;
import com.modulytic.dalia.smpp.internal.PduBridge;
import net.gescobar.smppserver.packet.GenericSubmit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SmPersister {
    private SmPersister() {}

    public static void save(PduBridge<? extends GenericSubmit> sm, List<AppAddress> dests, String messageId, String smppUser) {
        final RegisteredDelivery registeredDelivery = sm.getRegisteredDelivery();
        final Map<String, Object> values = new ConcurrentHashMap<>();

        for (AppAddress dest : dests) {
            values.clear();
            values.put(DatabaseConstants.MSG_ID, messageId);
            values.put(DatabaseConstants.SOURCE_ADDR, dest);
            values.put(DatabaseConstants.DEST_ADDR, sm.getSourceAddress());
            values.put(DatabaseConstants.FAILURE_ONLY, registeredDelivery.getFailureOnly());
            values.put(DatabaseConstants.INTERMEDIATE, registeredDelivery.getIntermediate());
            values.put(DatabaseConstants.SMPP_USER, smppUser);

            Context.getDatabase().insert(DatabaseConstants.DLR_TABLE, values);
        }
    }
}
