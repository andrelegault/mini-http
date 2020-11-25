package com.comp445.udp.server;

import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

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

    // DatagramSocket
    private DatagramSocket datagramSocket;

    protected static volatile ConcurrentHashMap<Path, AtomicInteger> locks = new ConcurrentHashMap<Path, AtomicInteger>();

    private static final Logger logger = Logger.getLogger(Server.class.getName());

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

    private void run() throws Exception {
        try {
            datagramSocket = new DatagramSocket(this.port);
            log("Server successfully started!");
            log("Listening on port: " + this.port + " | Data directory: " + dataDir.toString());
            while (true) {
                processRequest();
            }

        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private void log(final String output) {
        logger.log(Level.INFO, "[localhost:" + this.port + "] => " + output);
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
