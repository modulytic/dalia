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
    private static final DLRUpdateHandler dlrUpdateHandler = new DLRUpdateHandler();

    /**
     * Route incoming messages
     * @param message   Parsed message, received from WebSockets
     */
    public void onMessage(WsdMessage message) {
        switch (message.getName()) {
            case "dlr_update.php":              // legacy
            case "dlr_update": {
                Map<String, ?> params = message.getParams();

                String id           = (String) params.get("id");
                String statusRaw    = (String) params.get("new_status");
                MessageState status = MessageState.fromCode(statusRaw);

                dlrUpdateHandler.updateStatus(id, status);
                break;
            }

            default:
                break;
        }
    }
}
