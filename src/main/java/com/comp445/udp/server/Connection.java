package com.comp445.udp.server;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.comp445.udp.PacketBuffer;

public class Connection {
    /**
     * A single request (potentially formed of multiple packets) needs a state. This
     * is the class representing this state.
     */


    private boolean connected = false;

    /// Packet's that were received from the peer.

    // Holds the packets that need to be ack'ed by the remote client.
    public static volatile PacketBuffer sent;

    // Holds the packets that need to be ack'ed by this instance.
    public static volatile PacketBuffer received;

    public PipedInputStream in;
    public PipedOutputStream out;
    public RequestHandler handler;

    public Connection() throws IOException {
        out = new PipedOutputStream();
        in = new PipedInputStream(out);
    }

    public void setHandler(RequestHandler handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
