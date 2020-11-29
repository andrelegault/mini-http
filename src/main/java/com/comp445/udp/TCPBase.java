package com.comp445.udp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

import com.comp445.udp.server.Connection;

public class TCPBase {
    /**
     * Takes care of updating buffers
     */
    public static Packet receivePacket(final DatagramChannel channel, final Selector selector, final ByteBuffer buf)
            throws IOException {
        buf.clear();
        selector.select();
        channel.receive(buf);
        buf.flip();
        final Packet p = Packet.fromBuffer(buf);
        buf.flip();
        return p;
    }

    public static void process(Connection conn, Packet p) throws IOException {
        final int type = p.getType();
        if (type == 1) { // ACK
            TCPSender.process(conn.sent, p);
        } else if (type == 4) {// DATA
            TCPReceiver.process(conn.received, p, conn.out);
        } else
            return;
    }
}
