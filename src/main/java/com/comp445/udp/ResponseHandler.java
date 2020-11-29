package com.comp445.udp;

import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class ResponseHandler extends Thread {
    /**
     * This object sends a new DATA packet until it's ack'ed.
     */

    public static final int WAIT_TIME = 500;
    final DatagramChannel channel;
    final Selector selector;
    final Packet packet;
    long checkFor;
    final PacketBuffer buffer;

    public ResponseHandler(final DatagramChannel channel, final Selector selector, final PacketBuffer buffer,
            final Packet packet) {
        this.channel = channel;
        this.selector = selector;
        this.packet = packet;
        this.checkFor = packet.getSequenceNumber();
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            synchronized (buffer) {
                buffer.set(checkFor, packet);
            }
            boolean sendAgain = true;
            do {
                System.out.println("Sending: " + packet);
                System.out.println("could be deadlocked if this is spammed");
                channel.send(packet.toBuffer(), Router.ADDRESS);
                packet.sent = true;
                Thread.sleep(WAIT_TIME);

                synchronized (buffer) {
                    sendAgain = !buffer.get(checkFor).acked;
                }
            } while (sendAgain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
