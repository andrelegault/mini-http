package com.comp445.udp;

import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class ResponseHandler extends Thread {
    /**
     * This object sends a new DATA packet until it's ack'ed.
     */

    // maybe make a counter that checks how many times something was sent, to not
    // send stuff forever.
    private static final int WAIT_TIME = 5000;
    final DatagramChannel channel;
    final Selector selector;
    final Packet packet;
    final long checkFor;
    final PacketBuffer buffer;

    public ResponseHandler(final DatagramChannel channel, final Selector selector, final PacketBuffer buffer,
            final Packet packet) {
        this.channel = channel;
        this.selector = selector;
        this.packet = packet;
        this.checkFor = packet.getSequenceNumber() - 1;// -2 b/c of 2 starting packets and +1 because its receiver sends
                                                       // back ack=seq+1
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            boolean sendAgain = true;
            do {
                System.out.println("Sending data packet!");
                channel.send(packet.toBuffer(), Router.ADDRESS);
                packet.sent = true;
                selector.select(ResponseHandler.WAIT_TIME);

                sendAgain = buffer.get(checkFor).acked;
            } while (sendAgain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
