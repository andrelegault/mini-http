package com.comp445.udp;

import java.util.HashMap;
import java.util.Map;

public class Connection {
    /**
     * A single request (potentially formed of multiple packets) needs a state. This
     * is the class representing this state.
     */

    /// How many elements the window may contain
    private static final int WINDOW_SIZE = 100;

    private boolean connected = false;

    /// Last managed packet received
    protected ManagedPacket lastReceived;

    /// ManagedPacket's that were sent to the peer.
    protected final Map<Long, ManagedPacket> sent = new HashMap<Long, ManagedPacket>();

    /// ManagedPacket's that were received from the peer.
    protected final Map<Long, ManagedPacket> received = new HashMap<Long, ManagedPacket>();

    /// Bunch of packets that may or may not have been ack'ed
    protected final ManagedPacket[] window = new ManagedPacket[WINDOW_SIZE];

    // Current location of the window
    protected final int current = 0;

    public Connection() {
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Map<Long, ManagedPacket> getReceived() {
        return this.received;
    }

    public Map<Long, ManagedPacket> getSent() {
        return this.sent;
    }
}
