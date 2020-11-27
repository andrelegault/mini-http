package com.comp445.udp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

import com.comp445.udp.Packet;
import com.comp445.udp.Router;

public class SenderPacketHandler extends Thread {
    private static volatile int WAIT_TIME = 5000;
    final DatagramChannel channel;
    final Selector selector;
    final Packet packet;
    final int checkFor;

    public SenderPacketHandler(final DatagramChannel channel, final Selector selector, final Packet packet) {
        this.channel = channel;
        this.selector = selector;
        this.packet = packet;
        this.checkFor = (int) packet.getSequenceNumber() - 2;
    }

    private void sendPacket(final Packet p, InetSocketAddress to) throws IOException {
        System.out.println("Sending a packet of type " + p.getType());
        channel.send(p.toBuffer(), to);
    }

    @Override
    public void run() {
        try {
            boolean sendAgain = true;
            do {
                sendPacket(packet, Router.ADDRESS);
                selector.select(SenderPacketHandler.WAIT_TIME);

                final Packet p;
                synchronized ((p = Client.sent.get(checkFor))) {
                    sendAgain = p.acked;
                }
            } while (sendAgain);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
