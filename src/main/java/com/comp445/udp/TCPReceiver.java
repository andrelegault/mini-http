package com.comp445.udp;

import java.io.IOException;
import java.io.PipedOutputStream;

public class TCPReceiver {

    public static void process(PacketBuffer buffer, Packet p, PipedOutputStream out) {
        long position = p.getSequenceNumber();
        if (position < 0 || position > buffer.getLength() - 1 || p.getPayload() == null || buffer.get(position) != null)
            return;

        buffer.set(position, p);
        synchronized (buffer.window) {
            if (position == buffer.window.start()) {
                for (int i = buffer.window.start(); i <= buffer.window.end(); i++) {
                    Packet current = buffer.get(i);
                    if (current == null)
                        break;
                    try {
                        out.write(current.getPayload());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    buffer.window.incr(); // move window by 1
                }
            }
        }
    }
}
