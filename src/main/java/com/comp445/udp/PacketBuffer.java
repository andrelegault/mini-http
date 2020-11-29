package com.comp445.udp;

import java.util.concurrent.atomic.AtomicInteger;

public class PacketBuffer {
    private static final int DEFAULT_SIZE = 1024;
    private final Packet[] buffer;
    public final Window window;
    public AtomicInteger last = new AtomicInteger(0);

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
        return buffer[last.intValue()];
    }

    public Packet get(long index) {
        return buffer[(int) index];
    }

    public int getLength() {
        return buffer.length;
    }

    // means the whole window is acked
    public boolean isWindowAcked() {
        for (int i = window.position(); i < window.end() && i < buffer.length; i++) {
            if (get(i) == null || !get(i).acked)
                return false;
        }
        return true;
    }

    public void set(long i, Packet p) {
        buffer[(int) i] = p;
    }

    public synchronized void incr() {
    }

    public synchronized boolean canConsumeWindow() {
        // TODO: could be `i <= window.end()`
        // what if it's just 1 packet? you wont be able to consume the window obviously
        // meaning youll never consume it
        for (int i = window.start(); i <= window.end(); i++) {
            if (get(i) == null)
                return false;
        }
        return true;
    }

}
