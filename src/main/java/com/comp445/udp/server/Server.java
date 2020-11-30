package com.comp445.udp.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.comp445.udp.Packet;
import com.comp445.udp.PacketBuffer;
import com.comp445.udp.RequestHandler;
import com.comp445.udp.ResponseHandler;
import com.comp445.udp.Router;
import com.comp445.udp.TCPBase;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;

/**
 * This class represents the httpc server.
 *
 */
public class Server {
    // Regex that matches any valid http request header
    private final String[] args;

    // Object that parses arguments provided through the CLI
    private final CommandLineParser parser = new DefaultParser();

    // Object that prints the format of the CLI
    private final HelpFormatter formatter = new HelpFormatter();

    // Contains all options
    private final Options options = new Options();

    // Contains all parsed arguments
    private CommandLine cmdLine;

    // Current directory of the server.
    final static String cwd = System.getProperty("user.dir");

    // Existence of verbose option
    protected boolean verbose = false;

    // Port number used.
    public int port = 8080;

    // Path to desired endpoint.
    public Path dataDir = Paths.get(cwd, "/DATA");

    public static volatile ConcurrentHashMap<Path, AtomicInteger> locks = new ConcurrentHashMap<Path, AtomicInteger>();

    /// Holds the connection for each socket address
    public static volatile ConcurrentHashMap<InetSocketAddress, Connection> connections = new ConcurrentHashMap<InetSocketAddress, Connection>();

    private Selector selector;

    /**
     * @param args Arguments.
     */
    public Server(final String[] args) {

        this.args = args;
        prepareOptions();
        try {
            parse(args);
            run();
        } catch (Exception e) {
            e.printStackTrace();
            formatter.printHelp("httpfs is a simple file server.", options);
        }
    }

    private void setPort() throws Exception {
        if (cmdLine.hasOption("p")) {
            this.port = Integer.parseInt(cmdLine.getOptionValue("p"));
            if (port < 1024 || port > 65535) {
                throw new Exception("port must be within 1024 and 65535");
            }
        }
    }

    private void setDataDir() throws Exception {
        final String valRes = cmdLine.getOptionValue("d");
        if (valRes != null) {
            if (valRes.length() >= 1) {
                final Path path = Paths.get(cwd, valRes);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    this.dataDir = path;
                } else {
                    throw new Exception("Invalid data directory");
                }
            } else {
                throw new Exception("Invalid data directory");
            }
        }
    }

    private void parse(final String[] args) throws Exception {
        for (final String s : args) {
            if (s.equalsIgnoreCase("help")) {
                throw new Exception();
            }
        }
        cmdLine = parser.parse(options, args);
        setPort();
        setDataDir();
        setVerbose();
    }

    private void setVerbose() {
        this.verbose = cmdLine.hasOption("v");
    }

    private void prepareOptions() {
        final Option optVerbose = Option.builder("v").argName("verbose").required(false).hasArg(false)
                .desc("Prints debugging messages.").build();
        final Option optPort = Option.builder("p").argName("port").required(false).hasArg(true).desc(
                "Specifies the port number that the server will listen and serve at. Values should be between 1024 and 65535. Default is 8080.")
                .build();
        final Option optDataDir = Option.builder("d").argName("Data directory").required(false).hasArg(true).desc(
                "Specifies the data directory used for serving files. Provided path is relative to the current directory and must exist. Default is DATA.")
                .build();
        options.addOption(optVerbose);
        options.addOption(optPort);
        options.addOption(optDataDir);
    }

    private InetSocketAddress getClientSocketAddress(final InetAddress address, final int port) {
        return new InetSocketAddress(address, port);
    }

    private void run() {
        // TODO: when everything is acked (in sent), close the connection via FIN...
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.bind(new InetSocketAddress(this.port));
            channel.register(selector, OP_READ);
            log("Listening on port: " + this.port + " | Data directory: " + dataDir.toString());

            final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

            for (;;) {
                Packet packet = TCPBase.receivePacket(channel, selector, buf);
                log("RECEIVED: " + packet);
                final InetSocketAddress key = getClientSocketAddress(packet.getPeerAddress(), packet.getPeerPort());

                Connection conn = connections.get(key);
                if (conn == null) {
                    packet = establishConnection(channel, buf, packet);
                    buf.flip();
                    if (packet != null) {
                        conn = new Connection();
                        conn.setConnected(true);
                        conn.sent = new PacketBuffer();
                        connections.put(key, conn);
                        if (packet.getType() == 1)
                            continue;
                        // there was an error where we would mistake handshake ACK for a legitimate ACK.
                    }
                }

                if (conn.isConnected()) {
                    TCPBase.process(conn, packet);
                    if (packet.getType() == 4) { // DATA
                        final Packet resp = Packet.buildAck(packet);
                        log("Sending " + resp);
                        channel.send(resp.toBuffer(), Router.ADDRESS);

                        // have NOT yet tested for multiple-packet requests
                        // initialized thread handling DATA request
                        if (conn.handler == null) {
                            if (conn.in.available() == 0)
                                continue;
                            conn.setHandler(new Thread(
                                    new RequestHandler(channel, selector, key, conn.in, this.verbose, this.dataDir)));
                            conn.handler.start();
                        } else {
                            if (conn.in.available() > 0) {
                                synchronized (conn.handler) {
                                    conn.handler.notify();
                                }
                            }
                        }
                    }
                    if (conn.sent.isAllAcked()) {
                        closeHandshake(channel, key);
                        channel.close();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeHandshake(final DatagramChannel channel, InetSocketAddress address) throws IOException {
        final int randomSeq = new Random().nextInt(1000);
        final Packet fin = new Packet.Builder().setType(5).setSequenceNumber(randomSeq)
                .setPeerAddress(address.getAddress()).setPortNumber(address.getPort()).setPayload(null).build();

        final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        channel.send(fin.toBuffer(), Router.ADDRESS);

        Packet ack = null;
        boolean ackReceived = false;
        while (!ackReceived) {
            // try to get a key but wait for a maximum of 5 seconds
            final Set<SelectionKey> keys = selector.selectedKeys();
            do {
                System.out.println("(closing) Received " + ack);
                channel.send(fin.toBuffer(), Router.ADDRESS);
                selector.select(ResponseHandler.WAIT_TIME);
            } while (keys.isEmpty());
            // looks like we got a bite!! what is it??
            buf.clear();
            // theres an overflow here, the channel contains more than a single request
            channel.receive(buf);
            if (buf.remaining() == Packet.MAX_LEN)
                continue;

            buf.flip();
            ack = Packet.fromBuffer(buf);
            buf.flip();
            System.out.println("(closing) RECEIVED: " + ack);

            if ((ack.getType() == 5) && ack.getSequenceNumber() == fin.getSequenceNumber() + 1) {
                ackReceived = true;
            }
        }
        System.out.println("Client knows we're trying to disconnect!");
        channel.send(ack.toBuffer(), Router.ADDRESS);
    }

    private Packet establishConnection(final DatagramChannel channel, final ByteBuffer buf, Packet syn)
            throws IOException {

        if (syn.getType() != 0 && syn.getSequenceNumber() != 0L)
            return null;

        final Packet synAck = syn.toBuilder().setSequenceNumber(1L).setType(2).setPayload(null).build();

        channel.send(synAck.toBuffer(), Router.ADDRESS);

        Packet ackOrData = null;
        boolean ackOrDataReceived = false;
        while (!ackOrDataReceived) {
            // try to get a key but wait for a maximum of 5 seconds
            final Set<SelectionKey> keys = selector.selectedKeys();
            do {
                System.out.println("Sending: " + synAck);
                channel.send(synAck.toBuffer(), Router.ADDRESS);
                selector.select(ResponseHandler.WAIT_TIME);
            } while (keys.isEmpty());
            // looks like we got a bite!! what is it??
            buf.clear();
            // theres an overflow here, the channel contains more than a single request
            channel.receive(buf);
            if (buf.remaining() == Packet.MAX_LEN)
                continue;

            buf.flip();
            ackOrData = Packet.fromBuffer(buf);
            buf.flip();
            System.out.println("RECEIVED: " + ackOrData);

            if ((ackOrData.getType() == 1 || ackOrData.getType() == 4) && ackOrData.getSequenceNumber() <= 1) {
                ackOrDataReceived = true;
            }
        }
        System.out.println("Client connected!");
        return ackOrData;
    }

    private void log(final String output) {
        System.out.println("[localhost:" + this.port + "] => " + output);
    }

    public static void main(String[] args) {
        final Server server = new Server(args);
    }
}
