package com.comp445.udp.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Formatter;
import java.util.Map;

public abstract class Request {
    /**
     * This class represents a request made by httpc. Only GET/POST requests are to
     * be supported.
     * 
     * Methods implemented follow https://tools.ietf.org/html/rfc7230#section-3.2
     */
    protected OutputStream out;
    private URL url;
    private Map<String, String> headers;
    protected byte[] data;
    private final boolean verbose;
    private final String outputFilename;
    private static final int DEFAULT_PORT = 80;

    private final StringBuilder verboseContainer = new StringBuilder();
    protected final Formatter outFmt = new Formatter(verboseContainer);

    protected Request(final URL target, final Map<String, String> headers, final boolean verbose,
            final String outputFilename, final byte[] data) throws IOException {
        this.url = target;
        this.headers = headers;
        this.verbose = verbose;
        this.outputFilename = outputFilename;
        this.data = data;

        setRequestHeaders();
    }

    protected abstract String getMethod();

    private void writeToFile(String sent, String received) throws IOException {
        final File output = new File(this.outputFilename);
        if (output.createNewFile()) {
            final FileWriter fileWriter = new FileWriter(output);
            fileWriter.write(sent);
            fileWriter.write(received);
            fileWriter.close();
        }
    }

    // TODO: establish connection (using handshake)
    // TODO: create request
    // TODO: create packet from request
    // TODO: send packet

    public byte[] toBytes() {
        final byte[] headerBytes = outFmt.toString().getBytes();
        final ByteBuffer buf = ByteBuffer.allocate(headerBytes.length + (data != null ? data.length : 0))
                .order(ByteOrder.BIG_ENDIAN);
        buf.put(ByteBuffer.wrap(headerBytes));
        if (data != null)
            buf.put(ByteBuffer.wrap(data));
        return buf.array();
    }

    private void close() throws IOException {
        outFmt.close();
        verboseContainer.setLength(0);
    }

    private String getQueryOrEmptyString() {
        final String query = url.getQuery();
        return (query != null && !query.equals("") && !query.equals("/")) ? "?" + url.getQuery() : "";
    }

    private String sanitizePath(String unsanitizedPath) {
        return unsanitizedPath.equals("") ? "/" : unsanitizedPath;
    }

    private void setRequestHeaders() {
        outFmt.format("%s %s%s HTTP/1.0\r%n", getMethod(), sanitizePath(url.getPath()), getQueryOrEmptyString());
        outFmt.format("Host: %s\r%n", url.getHost());
        outFmt.format("Upgrade-Insecure-Requests: 1\r%n");
        outFmt.format("Connection: Close\r%n");
        outFmt.format("Accept-Encoding: gzip, deflate, br\r%n");
        outFmt.format("DNT: 1\r%n");
        if (headers != null) {
            for (final Map.Entry<String, String> entry : headers.entrySet()) {
                outFmt.format("%s: %s\r%n", capitalize(entry.getKey()), entry.getValue());
            }
        }
        if (this instanceof PostRequest) {
            outFmt.format("Content-Type: text/plain\r%n");
            outFmt.format("Content-Length: %d\r%n", data == null ? 0 : data.length);
        }
        outFmt.format("\r%n");
    }

    private String capitalize(final String word) {
        return word == null || word.length() == 0 ? word : word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private String getURL(String response) {
        String[] lines = response.split(System.getProperty("line.separator"));
        String line = "";
        for (String s : lines) {
            if (s.startsWith("Location")) {
                line = s.substring(10);
            }
        }
        return line;
    }

}
