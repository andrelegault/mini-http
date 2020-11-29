package com.comp445.udp;

import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.ArrayList;

public class TCPSender {
    private TCPSender() {
    }

    // Sends DATA packets
    // Processes ACK packets
    private static Packet[] getReadyPackets(PacketBuffer buffer) {
        ArrayList<Packet> toSend = new ArrayList<Packet>();
        int temp = 0;
        // resending is taken care of by threads
        while (temp < Math.min(buffer.window.end(), buffer.getLength())) {
            final Packet p = buffer.get(temp);
            if (p != null && !buffer.get(temp).sent)
                toSend.add(buffer.get(temp));
            temp++;
        }
        // does this actually work?
        return toSend.toArray(new Packet[toSend.size()]);
    }

    public static void sendOutstanding(DatagramChannel channel, Selector selector, PacketBuffer buffer) {
        final Packet[] toSend = TCPSender.getReadyPackets(buffer);
        for (Packet p : toSend) {
            buffer.last.incrementAndGet();
            new Thread(new ResponseHandler(channel, selector, buffer, p)).start();
        }
    }

    // going to be called by the client (multi-threaded) + server (not
    // multi-threaded)
    //
    // basically sets the acked attribute of a packet.
    public static void process(PacketBuffer buffer, long relPosition) {
        // System.out.println("sent[" + relPosition + "].acked is now true");
        if (buffer.get(relPosition) == null) {
            System.out.println(relPosition);
        }
        buffer.get(relPosition).acked = true;
    }
}
