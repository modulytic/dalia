package com.modulytic.dalia.ws;

import com.modulytic.dalia.smpp.DLRUpdateHandler;
import com.modulytic.dalia.smpp.api.MessageState;
import com.modulytic.dalia.ws.api.WsdMessage;

import java.util.Map;

/**
 * Handle and route incoming WebSockets messages
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class WsdMessageHandler {
    /**
     * Used if the message name is "dlr_update" or "dlr_update.php"
     */
    private final DLRUpdateHandler dlrUpdateHandler;

    public WsdMessageHandler(DLRUpdateHandler updateHandler) {
        this.dlrUpdateHandler = updateHandler;
    }

    /**
     * Route incoming messages
     * @param message   Parsed message, received from WebSockets
     */
    public void onMessage(WsdMessage message) {
        Map<String, ?> params = message.getParams();

        switch (message.getName()) {
            case "dlr_update.php":              // legacy
            case "dlr_update": {
                String id           = (String) params.get("id");
                String statusRaw    = (String) params.get("new_status");
                MessageState status = MessageState.fromCode(statusRaw);

                this.dlrUpdateHandler.updateStatus(id, status);
                break;
            }
        }
    }
}
