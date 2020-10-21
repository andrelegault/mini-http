package com.comp445.httpc;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comp445.HttpcResponse;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;

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
    protected static final Pattern headerPattern = Pattern
            .compile("([Gg][Ee][Tt]|[Pp][Oo][Ss][Tt]) (\\/(\\w+\\/)*((\\w+\\.\\w+)|\\w+)) [Hh][Tt][Tt][Pp]\\/1\\.[10]");
    final String[] args;

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
    public String dataDir = Paths.get(cwd, "/DATA").toString();

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

    /**
     * Returns whether the provided http header is valid.
     * 
     * @param httpHeader First line of the http request.
     * @return true if the header is valid; false otherwise.
     */
    public static Matcher getHeaderMatcher(final String httpHeader) {
        final Matcher matcher = headerPattern.matcher(httpHeader);
        return matcher.find() ? matcher : null;
    }

    private void setDataDir() throws Exception {
        final String valRes = cmdLine.getOptionValue("d");
        if (valRes != null) {
            if (valRes.length() >= 1) {
                final Path path = Paths.get(cwd, valRes);
                final File file = new File(path.toString());
                if (file.exists() && file.isDirectory()) {
                    this.dataDir = path.toString();
                } else {
                    throw new Exception("Invalid data directory");
                }
            } else {
                throw new Exception("Invalid data directory");
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
            log("Server successfully started!");
            log("Listening on port: " + this.port + " | Data directory: " + dataDir);
            while (true) {
                waitForRequest();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void log(final String output) {
        System.out.println("[localhost:" + this.port + "] => " + output);
    }

    private void waitForRequest() throws Exception {
        log("Waiting for request...");
        final Socket socket = serverSocket.accept();
        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String inputLine, outputLine;

        String rawReq = "";
        while ((inputLine = in.readLine()) != null) {
            rawReq += inputLine + "\n";
            if (inputLine.equals("")) {
                log("Processing request...");
                break;
            }
        }

        final HttpcResponse res = getResponseFromRequest(rawReq);
        if (verbose) {
            log("Request Received...");
            System.out.println(rawReq);
        }

        final String sent = res.toString();

        out.write(sent);

        if (verbose) {
            log("Response sent...");
            System.out.println(sent + "\n");
        }

        out.close();
        in.close();
    }

    private HttpcResponse getResponseFromRequest(final String rawRequest) throws Exception {
        final String[] lines = rawRequest.split("\n");
        System.out.println(lines.length);
        final Matcher matcher = Server.getHeaderMatcher(lines[0]);
        if (matcher == null) {
            // request isnt proper format, return with 400
            return new HttpcResponse(400);
        } else {
            final String method = matcher.group(1);
            final String path = matcher.group(2);
            if (method.equalsIgnoreCase("GET")) {
                final Path readPath = Paths.get(dataDir, path);
                if (!Files.exists(readPath)) {
                    return new HttpcResponse(404);
                } else {
                    return new HttpcResponse(200, Files.readString(readPath));
                }
            } else if (method.equalsIgnoreCase("POST")) {
                final String content = extractBody(lines);
                writeStringToFile(path, content);
                return new HttpcResponse(201);
            } else {
                return new HttpcResponse(400);
            }
        }
    }

    private String extractBody(final String[] lines) {
        int bodyLocation = 0;
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            if (line.equals("")) {
                bodyLocation = i + 1;
                break;
            }
        }
        String body = "";
        for (int k = bodyLocation; k < lines.length; k++) {
            System.out.println(lines[k]);
            body += lines[k] + "\n";
        }
        return body;
    }

    private void writeStringToFile(final String path, final String content) throws Exception {
        final Path file = Paths.get(cwd, path);
        Files.write(file, content.getBytes());
    }

    public static void main(String[] args) {
        final Server server = new Server(args);
    }
}
