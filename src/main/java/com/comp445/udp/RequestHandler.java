package com.comp445.udp;

import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.comp445.udp.server.Response;
import com.comp445.udp.server.Server;

public class RequestHandler extends Thread {
    private static final Pattern headerPattern = Pattern.compile(
            "([Gg][Ee][Tt]|[Pp][Oo][Ss][Tt]) (\\/(\\w+\\/)*((\\w+\\.\\w+)|\\w+|(?!\\/))) [Hh][Tt][Tt][Pp]\\/1\\.[10]");
    private final boolean verbose;
    private final Path dataDir;
    private final InputStream in;
    private final InetAddress peer;
    private final static int NEWLINE = 0x0A;

    public RequestHandler(final InetAddress peer, final InputStream in, final boolean verbose, final Path dataDir) {
        System.out.println("New serverThread created!!");
        this.verbose = verbose;
        this.dataDir = dataDir;
        this.in = in;
        this.peer = peer;
    }

    private void log(final String output) {
        System.out.println("[thread " + Thread.currentThread().getId() + "] => " + output);
    }

    protected Map<String, String> extractHeaders(final InputStream in) throws Exception {
        final Map<String, String> headers = new HashMap<String, String>();
        String inputLine;
        while ((inputLine = readLine()) != null) {
            if (inputLine.equals("\r")) {
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

    protected String readLine() throws Exception {
        int c;
        String s = "";
        // WE SHOULD PAUSE HERE
        while ((c = in.read()) != -1) {
            if (c == NEWLINE) {
                break;
            } else {
                s += (char) c + "";
            }
            if (in.available() == 0) wait();
        }
        return s;
    }

    protected byte[] extractBody(final InputStream in, final int contentLength) throws Exception {
        final byte[] bytes = new byte[contentLength];
        for (int i = 0; i < contentLength; i++) {
            bytes[i] = (byte) in.read();
        }
        return bytes;
    }

    private void processRequest() throws Exception {
        if (verbose) {
            log("Request received from " + peer);
        }

        final Response res = getResponseFromRequest(in);

        if (verbose) {
            log("Request processed!");
        }

        final String sent = res.toString();
        System.out.println(res.toString());
        final byte[] bytes = res.body;

        // out.write(sent.getBytes());
        // if (bytes != null) {
        // for (final byte b : bytes) {
        // out.write((char) (b & 0xFF));
        // }
        // }

        if (verbose) {
            log("Response sent to " + peer);
            System.out.println(sent + "\n");
        }

        // in.close();
    }

    private Response getResponseFromRequest(final InputStream in) throws Exception {
        final String httpLine = readLine();
        final Matcher matcher = RequestHandler.getHeaderMatcher(httpLine);
        if (matcher == null) {
            // request isnt proper format, return with 400
            return new Response(400);
        } else {
            final String method = matcher.group(1);
            final String resource = matcher.group(2);
            final Map<String, String> headers = extractHeaders(in);
            final Path path = Paths.get(dataDir.toString(), resource);
            if (method.equalsIgnoreCase("GET")) {
                return processGet(path, resource);
            } else if (method.equalsIgnoreCase("POST")) {
                try {
                    final int contentLength = Integer.parseInt(headers.get("Content-Length"));
                    final byte[] body = extractBody(in, contentLength);
                    return processPost(path, body);
                } catch (Exception e) {
                    return new Response(400);
                }
            } else {
                return new Response(400);
            }
        }
    }

    private Response processGet(final Path path, final String resource) throws Exception {
        if (!Files.exists(path)) {
            return new Response(404);
        } else {
            if (Files.isReadable(path)) {
                // its readable
                if (Files.isDirectory(path)) {
                    // its a directory
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
                    return new Response(200, path, container.toString().getBytes());
                } else {
                    // its a file
                    return new Response(200, path, readUsingFileChannel(path));
                }
            } else {
                return new Response(403);
            }
        }
    }

    private byte[] readUsingFileChannel(final Path path) throws Exception {
        AtomicInteger check = Server.locks.get(path);
        while (check != null && check.get() == 0) {
            check = Server.locks.get(path);
        }
        Server.locks.putIfAbsent(path, new AtomicInteger(1));
        Server.locks.get(path).incrementAndGet();
        try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            final Future<Integer> operation = channel.read(buffer, 0);
            operation.get();
            final byte[] result = buffer.array();
            buffer.clear();

            return result;
        } finally {
            final int after = Server.locks.get(path).decrementAndGet();
            if (after == 0) {
                Server.locks.remove(path);
            }
        }
    }

    private void writeUsingFileChannel(final Path path, final byte[] data) throws Exception {
        AtomicInteger check = Server.locks.get(path);
        while (check != null) {
            check = Server.locks.get(path);
        }
        Server.locks.put(path, new AtomicInteger(0));
        try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE,
                StandardOpenOption.DSYNC, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            final ByteBuffer buff = ByteBuffer.wrap(data);
            final Future<Integer> operation = channel.write(buff, 0);
            buff.clear();
            operation.get();
        } finally {
            Server.locks.remove(path);
        }
    }

    private Response processPost(final Path path, final byte[] body) throws Exception {
        if (!Files.isDirectory(path)) {
            // its a file or doesn't exist
            if (Files.exists(path) && Files.isWritable(path)) {
                // is a writable file
                writeUsingFileChannel(path, body);
                return new Response(201);
            } else {
                if (!Files.exists(path)) {
                    // doesnt exist or is not writable
                    final Path parentPath = path.getParent();
                    try {
                        Files.createDirectories(parentPath);
                        writeUsingFileChannel(path, body);
                        return new Response(201);
                    } catch (Exception e) {
                        return new Response(403);
                    }
                } else {
                    return new Response(403);
                }
            }
        } else {
            return new Response(403);
        }
    }

    @Override
    public void start() {
        try {
            processRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
