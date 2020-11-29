package com.comp445.udp;

public class Window {
    private final int bufferSize;
    public static final int SIZE = 10;

    // TODO: use atomic integer if errors occur
    private int position = 0;

    public Window(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int start() {
        return position;
    }

    public synchronized void incr() {
        position = (position + 1) % bufferSize;
    }

    public synchronized void incr(int by) {
        position = (position + by) % bufferSize;
    }

    public int end() {
        return position + SIZE - 1;
    }

    public int position() {
        return position;
    }

    public synchronized boolean contains(int check) {
        // start should never >= to end() if things are done correctly
        return start() <= check && check <= end();
    }

    public synchronized void setPosition(int i) {
        position = i;
    }

}
