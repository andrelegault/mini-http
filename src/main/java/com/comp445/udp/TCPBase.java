package com.comp445.udp;

import java.io.PipedOutputStream;

import com.comp445.udp.server.Connection;

public class TCPBase {
    /**
     * Takes care of updating buffers
     */

    public static void process(Connection conn, Packet p, PipedOutputStream out) {
        PacketBuffer buffer;
        final int type = p.getType();
        // apparently this shouldnt be decreased for clients????????
        if (type == 1) { // ACK
            buffer = conn.sent;
            long relPosition = p.getSequenceNumber() - 1;
            if (relPosition < 0 || relPosition > buffer.getLength() - 1)
                return;
            TCPSender.process(buffer, relPosition);
        } else if (type == 4) {// DATA
            buffer = conn.received;
            long position = p.getSequenceNumber();
            if (position < 0 || position > buffer.getLength() - 1)
                return;
            TCPReceiver.process(buffer, p, out);
        } else
            return;
        if (buffer.isWindowAcked())
            buffer.window.incr(Window.SIZE); // not sure about that
        else if (p.getSequenceNumber() == buffer.window.position())
            buffer.window.incr(); // move window by 1
    }
}
