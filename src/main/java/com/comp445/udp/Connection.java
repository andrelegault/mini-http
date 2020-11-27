package com.comp445.udp;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.comp445.udp.server.ServerThread;

public class Connection {
    /**
     * A single request (potentially formed of multiple packets) needs a state. This
     * is the class representing this state.
     */

    /// How many elements the window may contain
    private static final int WINDOW_SIZE = 100;

    private boolean connected = false;

    /// Packet's that were sent to the peer.
    public final Map<Long, Packet> sent = new HashMap<Long, Packet>();

    /// Packet's that were received from the peer.
    public final Map<Long, Packet> received = new HashMap<Long, Packet>();

    /// Bunch of packets that may or may not have been ack'ed
    protected final Packet[] window = new Packet[WINDOW_SIZE];

    // Current location of the window
    public final int current = 0;

    public final ByteBuffer data = ByteBuffer.allocate(4096);

    public PipedInputStream in;
    public PipedOutputStream out;
    public ServerThread handler;

    public Connection() throws IOException {
        out = new PipedOutputStream();
        in = new PipedInputStream(out);
    }

    public void setHandler(ServerThread handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
