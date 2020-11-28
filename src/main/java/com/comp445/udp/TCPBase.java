package com.comp445.udp;

import java.io.PipedOutputStream;

public class TCPBase {
    /**
     * Takes care of updating buffers
     */

    public static void process(PacketBuffer buffer, Packet p, PipedOutputStream out, boolean isClient) {
        final int type = p.getType();
        // apparently this shouldnt be decreased for clients????????
        long position = p.getSequenceNumber();
        if (!isClient) position -=1;
        if (position < 0 || position > buffer.getLength() - 1)
            return;
        if (type == 1) // ACK
            TCPSender.process(buffer, p);
        else if (type == 4) // DATA
            TCPReceiver.process(buffer, p, out, isClient);
        else
            return;
        if (buffer.isWindowAcked())
            buffer.window.incr(Window.SIZE); // not sure about that
        else if (position == buffer.window.position())
            buffer.window.incr(); // move window by 1
    }
}
