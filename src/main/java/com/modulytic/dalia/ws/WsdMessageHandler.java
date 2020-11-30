package com.modulytic.dalia.ws;

import com.modulytic.dalia.local.MySqlDbManager;
import com.modulytic.dalia.smpp.DLRUpdateHandler;
import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import com.modulytic.dalia.ws.api.WsdMessage;

import java.util.Map;

public class WsdMessageHandler {
    private final DLRUpdateHandler dlrUpdateHandler;

    public WsdMessageHandler(DaliaSmppSessionListener listener, MySqlDbManager database) {
        this.dlrUpdateHandler = new DLRUpdateHandler(database, listener);
    }

    public void onMessage(WsdMessage message) {
        Map<String, ?> params = message.getParams();

        switch (message.getName()) {
            case "dlr_update.php":              // legacy
            case "dlr_update": {
                String id     = (String) params.get("id");
                String status = (String) params.get("new_status");

                this.dlrUpdateHandler.updateStatus(id, status);
                break;
            }
        }
    }
}
