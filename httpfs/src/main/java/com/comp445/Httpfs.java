package com.comp445;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;

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
public class Httpfs {
    // Regex that matches any valid http request header
    protected static final Pattern headerPattern = Pattern.compile(
            "([Gg][Ee][Tt]|[Pp][Oo][Ss][Tt]) (\\/(\\w+\\/)*((\\w+\\.\\w+)|\\w+|(?!\\/))) [Hh][Tt][Tt][Pp]\\/1\\.[10]");
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
    public Path dataDir = Paths.get(cwd, "/DATA");

    // ServerSocket
    private ServerSocket serverSocket;

    /**
     * @param args Arguments.
     */
    public Httpfs(final String[] args) {
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
                throw new Exception("show usage");
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
            serverSocket = new ServerSocket(this.port);
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
        System.out.println("[localhost:" + this.port + "] => " + output);
    }

    private void processRequest() throws Exception {
        final Socket socket = serverSocket.accept();
        if (verbose) {
            log("Request received from " + socket.getInetAddress());
        }

        final OutputStream out = socket.getOutputStream();
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        final HttpcResponse res = getResponseFromRequest(in);

        if (verbose) {
            log("Request processed!");
        }

        final String sent = res.toString();
        final byte[] bytes = res.body;

        out.write(sent.getBytes());
        if (bytes != null) {
            for (final byte b : bytes) {
                out.write((char) (b & 0xFF));
            }
        }

        if (verbose) {
            log("Response sent to " + socket.getInetAddress());
            System.out.println(sent + "\n");
        }

        out.close();
        in.close();
    }

    protected static final Map<String, String> extractHeaders(final BufferedReader in) throws Exception {
        final Map<String, String> headers = new HashMap<String, String>();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.equals("")) {
                break;
            } else {
                final int firstColon = inputLine.indexOf(":");
                if (firstColon != -1) {
                    final String key = inputLine.substring(0, firstColon).trim();
                    final String val = inputLine.substring(firstColon + 1).trim();
                    headers.put(key, val);
                } else {
                    throw new Exception("Header name shouldn't contain a colon!");
                }
            }
        }
        return headers;
    }

    private static String extractBody(final BufferedReader in, final int contentLength) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            builder.append((char) in.read());
        }
        return builder.toString();
    }

    private HttpcResponse processGet(final Path path, final String resource) throws Exception {
        if (!Files.exists(path)) {
            return new HttpcResponse(404);
        } else {
            if (Files.isReadable(path)) {
                if (Files.isDirectory(path)) {
                    final Stream<Path> listOfFiles = Files.list(path);
                    final StringBuilder container = new StringBuilder();
                    final Formatter outFmt = new Formatter(container);
                    outFmt.format("Showing contents of %s%n", resource);
                    listOfFiles.filter(Files::isReadable).forEach(tempPath -> {
                        final String sign = Files.isDirectory(tempPath) ? "d" : "-";
                        try {
                            outFmt.format("%s %s: %d%n", sign, tempPath.getFileName(), Files.size(path));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    listOfFiles.close();
                    outFmt.close();
                    return new HttpcResponse(200, path, container.toString().getBytes());
                } else {
                    return new HttpcResponse(200, path, Files.readAllBytes(path));
                }
            } else {
                return new HttpcResponse(403);
            }
        }
    }

    private HttpcResponse processPost(final Path path, final String body) throws Exception {
        if (!Files.isDirectory(path)) {
            // its a file or doesn't exist
            if (Files.exists(path) && Files.isWritable(path)) {
                // is a writable file
                Files.write(path, body.getBytes());
                return new HttpcResponse(201);
            } else {
                if (!Files.exists(path)) {
                    // doesnt exist or is not writable
                    final Path parentPath = path.getParent();
                    try {
                        Files.createDirectories(parentPath);
                        Files.write(path, body.getBytes());
                        return new HttpcResponse(201);
                    } catch (Exception e) {
                        return new HttpcResponse(403);
                    }
                } else {
                    return new HttpcResponse(403);
                }
            }
        } else {
            return new HttpcResponse(403);
        }
    }

    private HttpcResponse getResponseFromRequest(final BufferedReader in) throws Exception {
        final String httpLine = in.readLine();
        final Matcher matcher = Httpfs.getHeaderMatcher(httpLine);
        if (matcher == null) {
            // request isnt proper format, return with 400
            return new HttpcResponse(400);
        } else {
            final String method = matcher.group(1);
            final String resource = matcher.group(2);
            final Map<String, String> headers = extractHeaders(in);
            final Path path = Paths.get(dataDir.toString(), resource);
            if (method.equalsIgnoreCase("GET")) {
                return processGet(path, resource);
            } else if (method.equalsIgnoreCase("POST")) {
                final int contentLength = Integer.parseInt(headers.get("Content-Length"));
                final String body = extractBody(in, contentLength);
                return processPost(path, body);
            } else {
                return new HttpcResponse(400);
            }
        }
    }

    public static void main(String[] args) {
        final Httpfs server = new Httpfs(args);
    }
}
