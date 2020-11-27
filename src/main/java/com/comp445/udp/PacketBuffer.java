package com.comp445.udp;

public class PacketBuffer {
    private static final int DEFAULT_SIZE = 1024;
    private final Packet[] buffer;
    public final Window window;
    public int lastSent = 0;

    public PacketBuffer() {
        buffer = new Packet[DEFAULT_SIZE];
        window = new Window(DEFAULT_SIZE);
    }

    public PacketBuffer(final Packet[] packets) {
        buffer = new Packet[packets.length];
        window = new Window(packets.length);
        for (int i = 0; i < packets.length; i++) {
            buffer[i] = packets[i];
        }
    }

    public Packet get() {
        return buffer[lastSent];
    }

    public Packet get(int index) {
        return buffer[index];
    }

    public int getLength() {
        return buffer.length;
    }

    public boolean isWindowAcked() {
        for(int i = window.position(); i < window.end() && i <buffer.length; i++) {
            if (!get(i).acked) return false;
        }
        return true;
    }
}
