package com.comp445.udp;

public class Window {
    private final int bufferSize;
    public static final int SIZE = 10;
    private int position = 0;

    public Window(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int start() {
        return position;
    }

    public void incr() {
        position = (position + 1) % bufferSize;
    }

    public void incr(int by) {
        position = (position + by) % bufferSize;
    }

    public int end() {
        return position + SIZE - 1;
    }

    public int position() {
        return position;
    }

    public void setPosition(int i) {
        position = i;
    }

}
