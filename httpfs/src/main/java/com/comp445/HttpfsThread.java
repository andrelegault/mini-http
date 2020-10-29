package com.comp445;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

public class HttpfsThread implements Runnable {
    private static final Pattern headerPattern = Pattern.compile(
            "([Gg][Ee][Tt]|[Pp][Oo][Ss][Tt]) (\\/(\\w+\\/)*((\\w+\\.\\w+)|\\w+|(?!\\/))) [Hh][Tt][Tt][Pp]\\/1\\.[10]");
    private final boolean verbose;
    private final Socket socket;
    private final Path dataDir;

    public HttpfsThread(final Socket socket, final boolean verbose, final Path dataDir) {
        this.socket = socket;
        this.verbose = verbose;
        this.dataDir = dataDir;
    }

    private void log(final String output) {
        System.out.println("[thread " + Thread.currentThread().getId() + "] => " + output);
    }

    protected Map<String, String> extractHeaders(final BufferedReader in) throws Exception {
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

    /**
     * Returns whether the provided http header is valid.
     * 
     * @param httpHeader First line of the http request.
     * @return true if the header is valid; false otherwise.
     */
    protected static Matcher getHeaderMatcher(final String httpHeader) {
        final Matcher matcher = headerPattern.matcher(httpHeader);
        return matcher.find() ? matcher : null;
    }

    protected String extractBody(final BufferedReader in, final int contentLength) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            builder.append((char) in.read());
        }
        return builder.toString();
    }

    private void processRequest() throws Exception {
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

    private HttpcResponse getResponseFromRequest(final BufferedReader in) throws Exception {
        final String httpLine = in.readLine();
        final Matcher matcher = HttpfsThread.getHeaderMatcher(httpLine);
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
        // TODO: use synchronization to prevent deadlocks and livelocks
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

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
