package com.comp445.udp.server;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.comp445.udp.PacketBuffer;

public class Connection {
    /**
     * A single request (potentially formed of multiple packets) needs a state. This
     * is the class representing this state.
     * 
     * We use a static size limit because the main server thread will be blocked in
     * order to pause execution when writing to the stream.
     * 
     * The following code sample displays an example of the phenomenon:
     * https://pastebin.com/ppUGNuEu
     */

    /// We don't support files over 1MB
    private static final int MAX_FILE_SIZE_IN_BYTES = 1048576;

    private boolean connected = false;

    /// Packet's that were received from the peer.

    // Holds the packets that need to be ack'ed by the remote client.
    public PacketBuffer sent;

    // Holds the packets that need to be ack'ed by this instance.
    public PacketBuffer received = new PacketBuffer();

    public PipedInputStream in;
    public PipedOutputStream out;
    public Thread handler;

    public Connection() throws IOException {
        out = new PipedOutputStream();
        in = new PipedInputStream(out, MAX_FILE_SIZE_IN_BYTES);
    }

    public void setHandler(Thread handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
