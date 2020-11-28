package com.comp445.udp.client;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.comp445.udp.Packet;
import com.comp445.udp.PacketBuffer;
import com.comp445.udp.ResponseHandler;
import com.comp445.udp.Router;
import com.comp445.udp.TCPBase;
import com.comp445.udp.TCPSender;
import com.comp445.udp.Window;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static java.nio.channels.SelectionKey.OP_READ;

public class Client {
    /**
     * 
     * Each sender needs a buffer of packets to get ack'ed (named `sent`). The
     * receiver needs a bunch of packets to ack (named `received`).
     * 
     * Because the client is both a sender and a receiver it needs 2 lists of
     * packets
     */

    // General usage instructions
    private final String usageGeneral = "Usage:\n".concat("   httpc command [arguments] URL\n")
            .concat("The commands are:\n").concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");

    private final HelpFormatter formatter = new HelpFormatter();

    // All valid actions
    private final Set<String> validActions = Set.of("post", "get");

    // Object that parses arguments provided through the CLI
    private final CommandLineParser parser = new DefaultParser();

    // Contains all options
    private final Options options = new Options();

    // Contains all parsed arguments
    private CommandLine cmdLine;

    // Contains the raw arguments provided
    private final String[] args;

    // Request created using provided arguments
    protected Request req;

    // Existence of verbose option
    protected boolean verbose = false;

    // Request headers
    protected Map<String, String> headers = new HashMap<String, String>();

    // Request data
    protected byte[] data;

    // Request action
    protected String action;

    // Request target
    protected URL target;

    // Filename to output to
    protected String outputFilename;

    private final Logger logger = Logger.getLogger("CLIENT");

    private Selector selector;

    /**
     * In TCP, every sender has a receiver.
     * 
     * Senders have a list of DATA packets that are to be acknowledged by the
     * receiver. Receivers respond when those packets are successfully obtained by
     * issuing ACK packets.
     * 
     * Because the `Client` class is both a sender and a receiver, it needs to
     * support sending and receiving operations and thus needs 2 lists of packets.
     * 
     * Because the `Server` class is both a sender and a receiver, it needs to
     * support sending and receiving operations and thus needs 2 lists of packets.
     */

    // Holds the packets that need to be ack'ed by the remote server.
    public static volatile PacketBuffer sent;

    // Holds the packets that need to be ack'ed by this instance.
    public static volatile PacketBuffer received;

    public PipedOutputStream out;
    public PipedInputStream in;

    /**
     * Constructor.
     * 
     * @param args Arguments provided through CLI.
     * @throws Exception
     */
    public Client(final String[] args) {
        this.args = args;
        out = new PipedOutputStream();
        try {
            in = new PipedInputStream(out);
            parse();
            run();
        } catch (Exception e) {
            if (this.action == null || this.action.equalsIgnoreCase("help")) {
                System.err.println(usageGeneral);
            } else {
                formatter.printHelp("httpc " + this.action + " [arguments] URL", options);
            }
            e.printStackTrace();
        }
    }

    /**
     * Represents whether a string is a valid request.
     * 
     * @param check String to check.
     * @return boolean Whether the check is a request.
     */
    private boolean isRequest(final String check) {
        if (check != null && !check.isEmpty()) {
            for (String validAction : validActions) {
                if (validAction.equalsIgnoreCase(check.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Adds the options necessary for both post and get.
     */
    private void prepareCommonOptions() {
        final Option optVerbose = Option.builder("v").argName("verbose").required(false).hasArg(false)
                .desc("Prints the detail of the response such as protocol, status, and headers.").build();
        final Option optHeaders = Option.builder("h").argName("header:val").required(false).numberOfArgs(2)
                .valueSeparator(':').desc("Associates headers to HTTP Request with the format 'key:value'.").build();
        final Option optOutputFilename = Option.builder("o").argName("filename").required(false).hasArg()
                .desc("Writes to the specified file").build();
        options.addOption(optVerbose);
        options.addOption(optHeaders);
        options.addOption(optOutputFilename);
    }

    /**
     * Prepares get options.
     */
    private void prepareGetOptions() {
        prepareCommonOptions();
    }

    /**
     * Prepares post options.
     */
    private void preparePostOptions() {
        prepareCommonOptions();
        final Option optDataString = Option.builder("d").required(false).hasArg(true)
                .desc("Associates an inline data to the body HTTP POST request.").build();
        final Option optDataFile = Option.builder("f").required(false).hasArg(true)
                .desc("Associates the content of a file to the body HTTP POST request.").build();
        options.addOption(optDataString);
        options.addOption(optDataFile);
    }

    /**
     * Sets the `headers` variable.
     * 
     */
    private void setHeaders() {
        final Properties properties = cmdLine.getOptionProperties("h");
        this.headers = new HashMap<String, String>((Map) properties);
    }

    /**
     * Sets the `data` variable.
     * 
     * @throws IOException
     */
    private void setData() throws IOException {
        if (cmdLine.hasOption("d")) {
            this.data = cmdLine.getOptionValue("d").getBytes();
        } else if (cmdLine.hasOption("f")) {
            this.data = loadFileContents(Path.of(cmdLine.getOptionValue("f")));
        }
    }

    /**
     * Loads a file's contents into a String.
     * 
     * @return String Contents of the file.
     * @throws IOException
     */
    private byte[] loadFileContents(Path filePath) throws IOException {
        return Files.readAllBytes(filePath);
    }

    /**
     * Sets the `verbose` variable.
     */
    private void setVerbose() {
        this.verbose = cmdLine.hasOption("v");
    }

    /**
     * Searches for a URL in the list of args (excluding options). Sets the target
     * if possible, throws otherwise.
     * 
     * @throws Exception
     */
    private void setTarget() throws Exception {
        final String testTarget = args[args.length - 1];
        if (args.length > 1 && args[args.length - 2] == "-h") {
            throw new Exception("Invalid target");
        }

        this.target = new URL(testTarget); // throws if invalid
    }

    /**
     * Sets the outputFilename.
     */
    private void setOutputFilename() {
        this.outputFilename = cmdLine.getOptionValue("o");
    }

    /**
     * Parses the arguments passed, and sets member variables accordingly.
     * 
     * @param args Arguments passed.
     * @throws ParseException
     */
    private void parse() throws Exception {
        final int argLen = args.length;

        if (argLen < 1) {
            throw new Exception("Insufficient arguments provided!");
        }

        this.action = args[0];
        if (this.action.equalsIgnoreCase("help")) {
            if (args[1].equalsIgnoreCase("get")) {
                prepareGetOptions();
                this.action = "get";
                throw new Exception();
            } else if (args[1].equalsIgnoreCase("post")) {
                preparePostOptions();
                this.action = "post";
                throw new Exception();
            } else {
                throw new Exception();
            }
        }

        if (isRequest(this.action)) {
            if (action.equalsIgnoreCase("get")) {
                prepareGetOptions();
                cmdLine = parser.parse(options, args);
                setTarget();
                setVerbose();
                setHeaders();
                setOutputFilename();
            } else if (action.equalsIgnoreCase("post")) {
                preparePostOptions();
                this.cmdLine = parser.parse(options, args);
                setTarget();
                final boolean hasJsonData = cmdLine.hasOption("d");
                final boolean hasFileData = cmdLine.hasOption("f");
                if ((!hasJsonData && !hasFileData) || (hasJsonData ^ hasFileData)) {
                    setVerbose();
                    setHeaders();
                    setData();
                    setOutputFilename();
                } else {
                    throw new Exception("Invalid use of -d or -f");
                }
            } else {
                throw new Exception("Invalid use of -d or -f");
            }
        }
    }

    // synchronous connection establishment
    private void establishConnection(final DatagramChannel channel) throws IOException {
        // SEND SYN

        final Packet syn = new Packet.Builder().setType(0).setSequenceNumber(0L).setPortNumber(this.target.getPort())
                .setPeerAddress(InetAddress.getByName(this.target.getHost())).build();

        boolean synAckReceived = false;
        while (!synAckReceived) {
            System.out.println("nope");
            // try to get a key but wait for a maximum of 5 seconds
            final Set<SelectionKey> keys = selector.selectedKeys();
            do {
                channel.send(syn.toBuffer(), Router.ADDRESS);
                selector.select(5000);
            } while (keys.isEmpty());
            // looks like we got a bite!! what is it??

            final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            channel.receive(buf);
            buf.flip();

            final Packet resp = Packet.fromBuffer(buf);

            // its a SYNACK!! yes!!
            if (resp.getSequenceNumber() == syn.getSequenceNumber() + 1 && resp.getType() == 2) {
                synAckReceived = true;
            }
        }

        // why am definitely ack'ing this!!
        final Packet ack = syn.toBuilder().setType(1).setSequenceNumber(2L).build();

        channel.send(ack.toBuffer(), Router.ADDRESS);
        selector.selectedKeys().clear();
    }

    private void connect() throws IOException {
        this.selector = Selector.open();
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            channel.register(selector, OP_READ);
            establishConnection(channel);
            logger.info("Connection established!");

            final Packet[] packets = Packet.toArray(ByteBuffer.wrap(this.req.toBytes()).order(ByteOrder.BIG_ENDIAN),
                    this.target.getHost(), this.target.getPort());
            sent = new PacketBuffer(packets);

            for (;;) {
                TCPSender.sendOutstanding(channel, selector, Client.sent);
                final Packet p = receivePacket(channel);
                System.out.println(new String(p.getPayload()));
                TCPBase.process(Client.sent, p, out, true);
                // do {
                // sendPacket(packet, Router.ADDRESS);
                // selector.select(5000);
                // } while (keys.isEmpty());
                // final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                // channel.receive(buf);
                // buf.flip();

                // final Packet resp = Packet.fromBuffer(buf);

                // // is valid response
                // if (resp.getSequenceNumber() == packet.getSequenceNumber()) {
                // System.out.println("valid response!");
                // // server is listening
                // } else {
                // System.out.println("Invalid response!");
                // }

            }
        }
    }

    private Packet receivePacket(final DatagramChannel channel) throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        selector.select();
        channel.receive(buf);
        System.out.println("Received packet!");
        // buf.flip();
        return Packet.fromBuffer(buf);
    }

    private void run() throws Exception {
        if (this.action.equalsIgnoreCase("get")) {
            this.req = new GetRequest(this.target, this.headers, this.verbose, this.outputFilename);
        } else if (this.action.equalsIgnoreCase("post")) {
            this.req = new PostRequest(this.target, this.headers, this.data, this.verbose, this.outputFilename);
        }
        if (this.req != null) {
            // TODO: handle connection in client
            connect();
        }
    }

    public static void main(final String[] args) {
        new Client(args);
    }
}
