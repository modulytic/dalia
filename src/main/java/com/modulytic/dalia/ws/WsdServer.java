package com.modulytic.dalia.ws;

import com.google.gson.Gson;
import com.modulytic.dalia.app.Constants;
import com.modulytic.dalia.ws.api.WsdMessage;
import com.modulytic.dalia.ws.api.WsdMessageCode;
import com.modulytic.dalia.ws.include.WsdStatusListener;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket server
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class WsdServer extends WebSocketServer {
    protected static final Logger LOGGER = LoggerFactory.getLogger(WsdServer.class);

    /**
     * Active WebSocket clients connected
     */
    private static final List<WebSocket> activeConnections = new ArrayList<>();

    /**
     * Client currently selected by round-robin distributor
     */
    private static int activeIndex;

    /**
     * Status listeners
     */
    private static final Map<String, WsdStatusListener> pendingStatuses = new ConcurrentHashMap<>();

    /**
     * Create a new WebSockets server that can start with .run()
     * @param port  port to start server on
     */
    public WsdServer(int port) {
        super(new InetSocketAddress(Constants.WSD_HOST_PORT, port));
        LOGGER.info(String.format("Attempting to start WebSocket server on port %d", port));
    }

    /**
     * Called when new WebSockets connection is opened
     * @param conn      connection
     * @param handshake {@link ClientHandshake} (unused)
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info(String.format("New client (%s) connected to WebSocket server", conn.getRemoteSocketAddress()));
        activeConnections.add(conn);
    }

    /**
     * Called when WebSockets connection is closed for any reason
     * @param conn      connection that was terminated
     * @param code      exit code
     * @param reason    reason for disconnect
     * @param remote    unused
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info(String.format("Client (%s) disconnected with code %d, reason: %s", conn.getRemoteSocketAddress(), code, reason));
        activeConnections.remove(conn);
    }

    /**
     * Parse message and pass to our WsdMessageHandler
     * @param conn      connection message is received from
     * @param message   text of message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info(String.format("Received message '%s' from %s, processing", message, conn.getRemoteSocketAddress()));

        final WsdMessage wsdMessage = new Gson().fromJson(message, WsdMessage.class);
        final Map<String, ?> params = wsdMessage.getParams();

        if ("&cmd".equals(wsdMessage.getName())) {
            if ("STATUS".equals(params.get("code"))) {
                Map<String, ?> data = (Map<String, ?>) params.get("data");

                final String id = (String) data.get("id");
                final int status = ((Double) data.get("status")).intValue();

                final WsdStatusListener listener = pendingStatuses.get(id);
                listener.onStatus(status);
                pendingStatuses.remove(id);
            }
        }
        else {
            WsdMessageHandler.onMessage(wsdMessage);
        }
    }

    /**
     * When error occurs with connection
     * @param conn  connection error occurred on
     * @param ex    the error that occurred
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error(String.format("ERROR on connection %s: %s", conn.getLocalSocketAddress(), ex));
    }

    /**
     * WebSocket server is started
     */
    @Override
    public void onStart() {
        LOGGER.info("WebSocket server started successfully");
    }

    /**
     * Send message to client, and do not advance the round-robin load distributor
     * @param message   message to send
     */
    @SuppressWarnings("unused")
    public static void sendCurrent(WsdMessage message, WsdStatusListener listener) {
        send(message, listener, false);
    }

    /**
     * Send message to client, and advance round-robin load distributor
     * @param message   message to send
     */
    public static void sendNext(WsdMessage message, WsdStatusListener listener) {
        send(message, listener,true);
    }

    /**
     * Send message to client
     * @param message   message to send
     * @param advance   if true: advance round-robin load distributor, if false do not
     */
    private static void send(WsdMessage message, WsdStatusListener listener, boolean advance) {
        if (advance)
            activeIndex++;

        if (activeIndex >= activeConnections.size())
            activeIndex = 0;

        try {
            WebSocket current = activeConnections.get(activeIndex);

            pendingStatuses.put(message.getId(), listener);
            current.send(message.toString());
        }
        catch (IndexOutOfBoundsException e) {
            // we have tested if activeIndex is greater than size, so this can only happen if size is actually 0
            listener.onStatus(WsdMessageCode.NO_CLIENTS);
        }
    }
}
