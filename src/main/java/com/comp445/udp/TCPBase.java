package com.comp445.udp;

import java.io.IOException;
import java.io.PipedOutputStream;
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
        return Packet.fromBuffer(buf);
    }

    public static void process(Connection conn, Packet p) throws IOException {
        PacketBuffer buffer;
        final int type = p.getType();
        // apparently this shouldnt be decreased for clients????????
        if (type == 1) { // ACK
            buffer = conn.sent;
            long relPosition = p.getSequenceNumber() - 1;
            if (relPosition < 0 || relPosition > buffer.getLength() - 1)
                return;
            TCPSender.process(buffer, relPosition);
        } else if (type == 4) {// DATA
            buffer = conn.received;
            long position = p.getSequenceNumber();
            if (position < 0 || position > buffer.getLength() - 1)
                return;
            TCPReceiver.process(buffer, p);
        } else
            return;
        if (buffer.isWindowAcked()) {
            if (p.getType() == 4) {
                for (int i = buffer.window.start(); i < buffer.window.end(); i++) {
                    try {
                        byte[] data = conn.received.get(i).getPayload();
                        for (byte b : data)
                            System.out.write(b);
                        conn.out.write(data);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            buffer.window.incr(Window.SIZE); // not sure about that
        } else if (p.getSequenceNumber() == buffer.window.position()) {
            if (p.getType() == 4) {
                byte[] data = conn.received.get(buffer.window.start()).getPayload();
                for (byte b : data)
                    System.out.write(b);
                conn.out.write(data);
            }
            buffer.window.incr(); // move window by 1
        }
    }
}
