package com.comp445;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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

public class HttpfsThread implements Runnable {
    private static final Pattern headerPattern = Pattern.compile(
            "([Gg][Ee][Tt]|[Pp][Oo][Ss][Tt]) (\\/(\\w+\\/)*((\\w+\\.\\w+)|\\w+|(?!\\/))) [Hh][Tt][Tt][Pp]\\/1\\.[10]");
    private final boolean verbose;
    private final Socket socket;
    private final Path dataDir;
    private final static int NEWLINE = 0x0A;

    public HttpfsThread(final Socket socket, final boolean verbose, final Path dataDir) {
        this.socket = socket;
        this.verbose = verbose;
        this.dataDir = dataDir;
    }

    private void log(final String output) {
        System.out.println("[thread " + Thread.currentThread().getId() + "] => " + output);
    }

    protected Map<String, String> extractHeaders(final InputStream in) throws Exception {
        final Map<String, String> headers = new HashMap<String, String>();
        String inputLine;
        while ((inputLine = readLine(in)) != null) {
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

    protected static String readLine(final InputStream inputStream) throws Exception {
        int c;
        String s = "";
        while ((c = inputStream.read()) != -1) {
            if (c == NEWLINE) {
                break;
            } else {
                s += (char) c + "";
            }
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
            log("Request received from " + socket.getInetAddress());
        }

        final OutputStream out = socket.getOutputStream();
        final InputStream in = socket.getInputStream();

        final HttpcResponse res = getResponseFromRequest(in);

        if (verbose) {
            log("Request processed!");
        }

        final String sent = res.toString();
        System.out.println(res.toString());
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

    private HttpcResponse getResponseFromRequest(final InputStream in) throws Exception {
        final String httpLine = readLine(in);
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
                try {
                    final int contentLength = Integer.parseInt(headers.get("Content-Length"));
                    final byte[] body = extractBody(in, contentLength);
                    return processPost(path, body);
                } catch (Exception e) {
                    return new HttpcResponse(400);
                }
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
                    return new HttpcResponse(200, path, container.toString().getBytes());
                } else {
                    // its a file
                    return new HttpcResponse(200, path, readUsingFileChannel(path));
                }
            } else {
                return new HttpcResponse(403);
            }
        }
    }

    private byte[] readUsingFileChannel(final Path path) throws Exception {
        AtomicInteger check = Httpfs.locks.get(path);
        while (check != null && check.get() == 0) {
            check = Httpfs.locks.get(path);
        }
        Httpfs.locks.putIfAbsent(path, new AtomicInteger(1));
        Httpfs.locks.get(path).incrementAndGet();
        try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer;
            if (channel.size() < 1024) {
                buffer = ByteBuffer.allocate((int) channel.size());
            } else {
                buffer = ByteBuffer.allocate(1024);
            }
            final Future<Integer> operation = channel.read(buffer, 0);
            operation.get();
            final byte[] result = buffer.array();
            buffer.clear();

            return result;
        } finally {
            final int after = Httpfs.locks.get(path).decrementAndGet();
            if (after == 0) {
                Httpfs.locks.remove(path);
            }
        }
    }

    private void writeUsingFileChannel(final Path path, final byte[] data) throws Exception {
        AtomicInteger check = Httpfs.locks.get(path);
        while (check != null) {
            check = Httpfs.locks.get(path);
        }
        Httpfs.locks.put(path, new AtomicInteger(0));
        try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE,
                StandardOpenOption.DSYNC, StandardOpenOption.CREATE)) {
            final ByteBuffer buff = ByteBuffer.wrap(data);
            final Future<Integer> operation = channel.write(buff, 0);
            buff.clear();
            operation.get();
        } finally {
            Httpfs.locks.remove(path);
        }
    }

    private HttpcResponse processPost(final Path path, final byte[] body) throws Exception {
        if (!Files.isDirectory(path)) {
            // its a file or doesn't exist
            if (Files.exists(path) && Files.isWritable(path)) {
                // is a writable file
                writeUsingFileChannel(path, body);
                return new HttpcResponse(201);
            } else {
                if (!Files.exists(path)) {
                    // doesnt exist or is not writable
                    final Path parentPath = path.getParent();
                    try {
                        Files.createDirectories(parentPath);
                        writeUsingFileChannel(path, body);
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
            e.printStackTrace();
        }
    }
}
