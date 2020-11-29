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

    public static void sendOutstanding(DatagramChannel channel, Selector selector, PacketBuffer buffer)
            throws InterruptedException {
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
    public static void process(PacketBuffer buffer, Packet p) {
        long relPosition = p.getSequenceNumber() - 1;
        System.out.println("sent[" + relPosition + "].acked is now true");
        if (relPosition < 0 || relPosition > buffer.getLength() - 1)
            return;
        synchronized (buffer.get(relPosition)) {
            buffer.get(relPosition).acked = true;
        }
        synchronized (buffer.window) {
            if (buffer.isWindowAcked())
                buffer.window.incr(Window.SIZE); // not sure about that
            else if (relPosition == buffer.window.start()) {
                for (int i = buffer.window.start(); i <= buffer.window.end(); i++) {
                    Packet current = buffer.get(i);
                    if (current == null || !current.acked)
                        break;
                    buffer.window.incr(); // move window by 1
                }
            }
        }
    }
}
