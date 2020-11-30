package com.modulytic.dalia.ws;

import com.google.gson.Gson;
import com.modulytic.dalia.ws.api.WsdMessage;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class WsdServer extends WebSocketServer {
    protected final Logger LOGGER = LoggerFactory.getLogger(WsdServer.class);

    private final List<WebSocket> activeConnections;
    private int activeIndex = 0;

    private WsdMessageHandler handler = null;

    public WsdServer(int port) {
        super(new InetSocketAddress("0.0.0.0", port));

        this.activeConnections = new ArrayList<>();
        LOGGER.info(String.format("Attempting to start WebSocket server on port %d", port));
    }

    public void setHandler(WsdMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info(String.format("New client (%s) connected to WebSocket server", conn.getRemoteSocketAddress()));
        this.activeConnections.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info(String.format("Client (%s) disconnected with code %d, reason: %s", conn.getRemoteSocketAddress(), code, reason));
        this.activeConnections.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info(String.format("Received message from %s, processing", conn.getRemoteSocketAddress()));

        if (this.handler != null) {
            WsdMessage wsdMessage = new Gson().fromJson(message, WsdMessage.class);
            this.handler.onMessage(wsdMessage);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error(String.format("ERROR on connection %s: %s", conn.getLocalSocketAddress(), ex));
    }

    @Override
    public void onStart() {
        LOGGER.info("WebSocket server started successfully");
    }

    public boolean sendCurrent(WsdMessage message) {
        return send(message, false);
    }

    public boolean sendNext(WsdMessage message) {
        return send(message, true);
    }

    private boolean send(WsdMessage message, boolean advance) {
        if (advance)
            activeIndex++;

        if (activeIndex >= activeConnections.size())
            activeIndex = 0;

        try {
            // actually do the sending
            WebSocket current = activeConnections.get(activeIndex);
            current.send(message.toString());
            LOGGER.info("Successfully sent message to client");

            return true;
        }
        catch (IndexOutOfBoundsException e) {
            // we have tested if activeIndex is greater than size, so this can only happen if size is actually 0
            LOGGER.info(String.format("Attempted to send message '%s', but no clients connected", message.getId()));
        }

        return false;
    }
}
