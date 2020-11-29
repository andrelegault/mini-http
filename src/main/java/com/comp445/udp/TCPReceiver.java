package com.comp445.udp;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.nio.channels.DatagramChannel;

public class TCPReceiver {

    // TODO: only process data
    public static void process(PacketBuffer buffer, Packet p) {
        long seq = p.getSequenceNumber();
        if (p.getPayload() == null || buffer.get(seq) != null) // no data or already processed
            return;

        buffer.set(seq, p);

        // change of plans
        // instead of checking if i can consume a window
        // ill just consume whatever i can from the window
        // meaning if the window starts with 3 packets
        // for (int i = buffer.window.start(); i < buffer.window.end(); i++) {
        //     try {
        //         if (buffer.get(i) == null) break;
        //         out.write(buffer.get(i).getPayload());
        //         // buffer.get(i).consumed = true;
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        // }
    }
}
