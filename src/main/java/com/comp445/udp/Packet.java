package com.comp445.udp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Packet represents a simulated network packet. As we don't have unsigned types
 * in Java, we can achieve this by using a larger type.
 */
public class Packet {
    public static final int MIN_LEN = 11;
    public static final int MAX_LEN = 11 + 1013;
    public boolean acked = false;
    public boolean consumed = false;
    public boolean sent = false;

    /**
     * Types can be one of the following: SYN, ACK, SYN-ACK, or NAK.
     * 
     * 0 indicates a SYN packet.
     * 
     * 1 indicates an ACK packet.
     * 
     * 2 indicates a SYN-ACK packet.
     * 
     * 3 indicates a NAK packet.
     * 
     * 4 indicates a DATA packet.
     * 
     * 5 indicates a FIN
     */
    private final int type;
    private final long sequenceNumber;
    private final InetAddress peerAddress;
    private final int peerPort;
    private final byte[] payload;

    private Packet(int type, long sequenceNumber, InetAddress peerAddress, int peerPort, byte[] payload) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        this.payload = payload;
    }

    public int getType() {
        return type;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public InetAddress getPeerAddress() {
        return peerAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public byte[] getPayload() {
        return payload;
    }

    /**
     * Creates a builder from the current packet. It's used to create another packet
     * by re-using some parts of the current packet.
     */
    public Builder toBuilder() {
        return new Builder().setType(type).setSequenceNumber(sequenceNumber).setPeerAddress(peerAddress)
                .setPortNumber(peerPort).setPayload(payload);
    }

    /**
     * Writes a raw presentation of the packet to byte buffer. The order of the
     * buffer should be set as BigEndian.
     */
    private void write(ByteBuffer buf) {
        buf.put((byte) type);
        buf.putInt((int) sequenceNumber);
        buf.put(peerAddress.getAddress());
        buf.putShort((short) peerPort);
        if (payload != null)
            buf.put(payload);
    }

    /**
     * Create a byte buffer in BigEndian for the packet. The returned buffer is
     * flipped and ready for get operations.
     */
    public ByteBuffer toBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        write(buf);
        buf.flip();
        return buf;
    }

    /**
     * Returns a raw representation of the packet.
     */
    public byte[] toBytes() {
        ByteBuffer buf = toBuffer();
        byte[] raw = new byte[buf.remaining()];
        buf.get(raw);
        return raw;
    }

    /**
     * fromBuffer creates a packet from the given ByteBuffer in BigEndian.
     */
    public static Packet fromBuffer(ByteBuffer buf) throws IOException {
        if (buf.limit() < MIN_LEN || buf.limit() > MAX_LEN) {
            throw new IOException("Invalid length");
        }

        Builder builder = new Builder();

        // get a single byte
        builder.setType(Byte.toUnsignedInt(buf.get()));

        // get 4 bytes (stored in long due to unsigned)
        builder.setSequenceNumber(Integer.toUnsignedLong(buf.getInt()));

        // get 4x1 byte = 4bytes
        byte[] host = new byte[] { buf.get(), buf.get(), buf.get(), buf.get() };
        builder.setPeerAddress(Inet4Address.getByAddress(host));

        // get 2 bytes (stored in int due to unsigned)
        builder.setPortNumber(Short.toUnsignedInt(buf.getShort()));

        // get the rest of the buffer (maximum 1013 bytes)
        byte[] payload = new byte[buf.remaining()];
        buf.get(payload);
        builder.setPayload(payload);

        return builder.build();
    }

    /**
     * fromBytes creates a packet from the given array of bytes.
     */
    public static Packet fromBytes(byte[] bytes) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        buf.put(bytes);
        buf.flip();
        return fromBuffer(buf);
    }

    public static Packet buildAck(Packet p) {
        // no need to change peer as it's done by the router
        long seq = p.getSequenceNumber();
        return p.toBuilder().setType(1).setSequenceNumber(seq + 1).setPayload(null).build();
    }

    public static Packet[] toArray(ByteBuffer buf, InetAddress peerAddress, int port) throws UnknownHostException {
        final int maxPayloadSize = Packet.MAX_LEN - Packet.MIN_LEN; // 1013
        final int numPackets = (int) Math.ceil((double) buf.capacity() / (maxPayloadSize));
        final Packet[] segmented = new Packet[numPackets];
        for (int i = 0; i < numPackets; i++) {
            final byte[] chunk = new byte[Math.min(maxPayloadSize, buf.remaining())];
            // final byte[] chunk = new byte[i == numPackets - 1 ? buf.capacity() -
            // buf.position() : maxPayloadSize];
            buf.get(chunk);
            // Here we're using i+1 because the first 1 sequence nubmers are reserved for
            // the handshake
            segmented[i] = new Packet.Builder().setType(4).setSequenceNumber(i).setPortNumber(port)
                    .setPeerAddress(peerAddress).setPayload(chunk).build();
        }
        return segmented;
    }

    @Override
    public String toString() {
        return String.format("%s #%d peer=%s:%d, size=%d", stringType(), sequenceNumber, peerAddress, peerPort,
                payload == null ? 0 : payload.length);
    }

    private String stringType() {
        String stringType = "";
        switch (type) {
            case 0:
                stringType = "SYN";
                break;
            case 1:
                stringType = "ACK";
                break;
            case 2:
                stringType = "SYNACK";
                break;
            case 3:
                stringType = "NAK";
                break;
            case 4:
                stringType = "DATA";
                break;
        }
        return stringType;
    }

    public static class Builder {
        private int type;
        private long sequenceNumber;
        private InetAddress peerAddress;
        private int portNumber;
        private byte[] payload;

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setSequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder setPeerAddress(InetAddress peerAddress) {
            this.peerAddress = peerAddress;
            return this;
        }

        public Builder setPortNumber(int portNumber) {
            this.portNumber = portNumber;
            return this;
        }

        public Builder setPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public Packet build() {
            return new Packet(type, sequenceNumber, peerAddress, portNumber, payload);
        }
    }

    public void process(final Packet p) {

    }

}
