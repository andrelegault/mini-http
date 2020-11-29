package com.comp445.udp.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.comp445.udp.Packet;
import com.comp445.udp.PacketBuffer;
import com.comp445.udp.RequestHandler;
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
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.bind(new InetSocketAddress(this.port));
            channel.register(selector, OP_READ);
            log("Listening on port: " + this.port + " | Data directory: " + dataDir.toString());

            final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

            for (;;) {

                final Packet packet = TCPBase.receivePacket(channel, selector, buf);
                log("RECEIVED: " + packet);
                final InetSocketAddress key = getClientSocketAddress(packet.getPeerAddress(), packet.getPeerPort());

                Connection conn = connections.get(key);
                if (conn == null) {
                    final boolean connected = establishConnection(channel, buf, packet);
                    if (connected) {
                        conn = new Connection();
                        conn.setConnected(true);
                        conn.sent = new PacketBuffer();
                        connections.put(key, conn);
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
                            // conn.setHandler(
                            new RequestHandler(channel, selector, key, conn.in, this.verbose, this.dataDir).start();
                            // conn.handler.start();
                        }
                        // else {
                        // synchronized (conn.handler) {
                        // conn.handler.notify();
                        // }
                        // }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean establishConnection(final DatagramChannel channel, final ByteBuffer buf, Packet syn)
            throws IOException {

        if (syn.getType() != 0 && syn.getSequenceNumber() != 0L)
            return false;

        final Packet synAck = syn.toBuilder().setSequenceNumber(1L).setType(2).build();

        Packet ack;
        do {
            channel.send(synAck.toBuffer(), Router.ADDRESS);
            this.selector.select(5000);
            buf.clear();
            channel.receive(buf);
            buf.flip();
            ack = Packet.fromBuffer(buf);
        } while (ack.getType() != 1 && ack.getSequenceNumber() != 1L);

        return true;
    }

    private void log(final String output) {
        System.out.println("[localhost:" + this.port + "] => " + output);
    }

    private void processRequest() throws Exception {
        // https://www.baeldung.com/udp-in-java
        // TODO: replace `accept` with 3-way handshake
        // final Socket socket = datagramSocket.accept();
        // new Thread(new ServerThread(socket, verbose, dataDir)).start();
    }

    public static void main(String[] args) {
        final Server server = new Server(args);
    }
}
