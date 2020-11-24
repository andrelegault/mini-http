package com.comp445.udp;

import java.nio.channels.DatagramChannel;

public class ManagedPacket {
    /**
     * Packet that includes things such as a timer, etc.
     */
    
    final Packet packet;
    final boolean acknowledged;

    // TODO: add timer
    public ManagedPacket(Packet packet, DatagramChannel channel) {
        this.packet = packet;
        this.acknowledged = false;
    }
    
}
