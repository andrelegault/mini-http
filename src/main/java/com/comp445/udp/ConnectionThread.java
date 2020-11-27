package com.comp445.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

import com.comp445.udp.client.Client;

public class ConnectionThread extends Thread {
    final DatagramChannel channel;
    final Selector selector;
    final Packet packet;

    public ConnectionThread(final DatagramChannel channel, final Selector selector, final Packet packet) {
        this.channel = channel;
        this.selector = selector;
        this.packet = packet;
    }

    private void sendPacket(final Packet p, InetSocketAddress to) throws IOException {
        System.out.println("Sending a packet of type " + p.getType());
        channel.send(p.toBuffer(), to);
    }

    @Override
    public void run() {
        try {
            Client.connections.putIfAbsent(packet.getPeerAddress(), new Connection());

            final Set<SelectionKey> keys = selector.selectedKeys();
            do {
                sendPacket(packet, Router.ADDRESS);
                selector.select(5000);
            } while (keys.isEmpty());

            // got a response
            final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            channel.receive(buf);
            buf.flip();

            final Packet resp = Packet.fromBuffer(buf);

            // is valid response
            if (resp.getSequenceNumber() == packet.getSequenceNumber()) {
                System.out.println("valid response!");
                // server is listening
                final Connection conn = Client.connections.get(resp.getPeerAddress());
                // conn.getSent().get(packet.getSequenceNumber()).acked = true;
                conn.received.put(resp.getSequenceNumber(), resp);
            } else {
                System.out.println("Invalid response!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
