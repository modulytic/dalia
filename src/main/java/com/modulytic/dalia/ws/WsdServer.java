package com.modulytic.dalia.ws;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
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
import java.util.HashMap;
import java.util.List;

/**
 * WebSocket server
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class WsdServer extends WebSocketServer {
    protected final Logger LOGGER = LoggerFactory.getLogger(WsdServer.class);

    /**
     * Active WebSocket clients connected
     */
    private final List<WebSocket> activeConnections;

    /**
     * Client currently selected by round-robin distributor
     */
    private int activeIndex = 0;

    /**
     * Handler for parsed messages
     */
    private WsdMessageHandler handler = null;

    /**
     * Status listeners
     */
    private final HashMap<String, WsdStatusListener> pendingStatuses;

    /**
     * Create a new WebSockets server that can start with .run()
     * @param port  port to start server on
     */
    public WsdServer(int port) {
        super(new InetSocketAddress("0.0.0.0", port));

        this.activeConnections = new ArrayList<>();
        this.pendingStatuses = new HashMap<>();

        LOGGER.info(String.format("Attempting to start WebSocket server on port %d", port));
    }

    /**
     * Set handler to deal with received messages
     * @param handler   a WsdMessageHandler
     */
    public void setHandler(WsdMessageHandler handler) {
        this.handler = handler;
    }

    /**
     * Called when new WebSockets connection is opened
     * @param conn      connection
     * @param handshake {@link ClientHandshake} (unused)
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info(String.format("New client (%s) connected to WebSocket server", conn.getRemoteSocketAddress()));
        this.activeConnections.add(conn);
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
        this.activeConnections.remove(conn);
    }

    /**
     * Parse message and pass to our {@link WsdMessageHandler}, {@link #handler}, if set
     * @param conn      connection message is received from
     * @param message   text of message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info(String.format("Received message '%s' from %s, processing", message, conn.getRemoteSocketAddress()));

        WsdMessage wsdMessage = new Gson().fromJson(message, WsdMessage.class);
        if (wsdMessage.getName().equals("&cmd")) {
            if (wsdMessage.getParams().get("code").equals("STATUS")) {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) wsdMessage.getParams().get("data");

                String id = (String) data.get("id");
                int status = ((Double) data.get("status")).intValue();

                pendingStatuses.get(id).onStatus(status);
                pendingStatuses.remove(id);
            }
        }
        else {
            if (this.handler != null)
                this.handler.onMessage(wsdMessage);
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
    public void sendCurrent(WsdMessage message, WsdStatusListener listener) {
        send(message, listener, false);
    }

    /**
     * Send message to client, and advance round-robin load distributor
     * @param message   message to send
     */
    public void sendNext(WsdMessage message, WsdStatusListener listener) {
        send(message, listener,true);
    }

    /**
     * Send message to client
     * @param message   message to send
     * @param advance   if true: advance round-robin load distributor, if false do not
     */
    private void send(WsdMessage message, WsdStatusListener listener, boolean advance) {
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
