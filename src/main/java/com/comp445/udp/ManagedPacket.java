package com.comp445.udp;

public class ManagedPacket {
    /**
     * Packet that includes the fields necessary to implement selective repeat.
     * Basically a wrapper class for Packet but with a state.
     */

    final protected Packet packet;
    public boolean acked;

    // TODO: add some sort of timer
    public ManagedPacket(Packet packet) {
        this.packet = packet;
        this.acked = false;
    }

}
