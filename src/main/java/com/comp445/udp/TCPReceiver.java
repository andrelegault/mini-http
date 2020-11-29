package com.comp445.udp;

import java.io.IOException;
import java.io.PipedOutputStream;

public class TCPReceiver {

    // TODO: only process data
    public static void process(PacketBuffer buffer, Packet p, PipedOutputStream out) {
        long position = p.getSequenceNumber();
        if (position < 0 || position > buffer.getLength() - 1 || p.getPayload() == null || buffer.get(position) != null)
            return;

        buffer.set(position, p);
        if (position == buffer.window.start()) {
            for (int i = buffer.window.start(); i <= buffer.window.end(); i++) {
                Packet current = buffer.get(i);
                if (current == null)
                    break;
                try {
                    out.write(current.getPayload());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                buffer.window.incr(); // move window by 1
            }
        }

        // change of plans
        // instead of checking if i can consume a window
        // ill just consume whatever i can from the window
        // meaning if the window starts with 3 packets
        // for (int i = buffer.window.start(); i < buffer.window.end(); i++) {
        // try {
        // if (buffer.get(i) == null) break;
        // out.write(buffer.get(i).getPayload());
        // // buffer.get(i).consumed = true;
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
    }
}
