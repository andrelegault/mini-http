package com.comp445.udp;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {
    /**
     * This class is used to manage packets sent by a server or client.
     * 
     * A request (or response) may contain numerous packets.
     */

    /**
     * Mapping of packet number to packet. The packet number is the same as the
     * sequence number because we don't base sequence numbers on payload size.
     */
    final Map<Integer, ManagedPacket> packets = new HashMap<Integer, ManagedPacket>();

    final ManagedPacket[] buffer = new ManagedPacket[WINDOW_SIZE];
    static final int WINDOW_SIZE = 10;

    PacketManager() {

    }

}
