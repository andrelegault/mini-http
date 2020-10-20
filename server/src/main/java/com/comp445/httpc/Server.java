package com.comp445.httpc;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class represents the httpc server.
 *
 */
public class Server {
    final String[] args;

    // Object that parses arguments provided through the CLI
    private final CommandLineParser parser = new DefaultParser();

    // Object that prints the format of the CLI
    private final HelpFormatter formatter = new HelpFormatter();

    // Contains all options
    private final Options options = new Options();

    // Contains all parsed arguments
    private CommandLine cmdLine;

    // Existence of verbose option
    protected boolean verbose = false;

    // Port number used.
    public int port = 8080;

    // Path to desired endpoint.
    public String dataDir = "/";

    // ServerSocket
    private ServerSocket serverSocket;

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
        }
    }

    private void setDataDir() throws Exception {
        final String valRes = cmdLine.getOptionValue("d");
        if (valRes != null) {
            if (valRes.length() >= 1) {
                if (valRes.charAt(0) != '/') {
                    throw new Exception("Invalid data directory");
                }
                if (valRes.length() >= 3 && valRes.substring(0, 3).equalsIgnoreCase("../")) {
                    throw new Exception("Invalid data directory!");
                } else {
                    this.dataDir = valRes;
                }
            }
        }
    }

    private void parse(final String[] args) throws Exception {
        cmdLine = parser.parse(options, args);
        setPort();
        setDataDir();
    }

    private void prepareOptions() {
        final Option optVerbose = Option.builder("v").argName("verbose").required(false).hasArg(false)
                .desc("Prints debugging messages.").build();
        final Option optPort = Option.builder("p").argName("port").required(false).hasArg(true)
                .desc("Specifies the port number that the server will listen and serve at. Default is 8080.").build();
        final Option optDataDir = Option.builder("d").argName("Data directory").required(false).hasArg(true)
                .desc("Specifies the port number that the server will listen and serve at. Default is 8080.").build();
        options.addOption(optVerbose);
        options.addOption(optPort);
        options.addOption(optDataDir);
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(this.port);
            Socket socket = serverSocket.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine, outputLine;
            String testStatusCode = "200";
            String testStatusMessage = "OK";

            // TODO: respond with correct status code
            outputLine = "HTTP/1.0 " + testStatusCode + " " + testStatusMessage;
            out.write(outputLine);

            while((inputLine = in.readLine()) != null) {
                if (inputLine.equals("\r\n")) {
                    break;
                }
            }

            out.close();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final Server server = new Server(args);
        System.out.println("Hello World!");
    }
}
