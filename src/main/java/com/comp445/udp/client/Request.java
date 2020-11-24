package com.comp445.udp.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
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
    private Socket socket;
    private InputStream in;
    // private BufferedReader in;
    protected byte[] data;
    private final boolean verbose;
    private final String outputFilename;
    private static final int DEFAULT_PORT = 80;

    private StringBuilder verboseContainer;
    protected Formatter outFmt;

    protected Request(final URL target, final Map<String, String> headers, final boolean verbose,
            final String outputFilename, final byte[] data) {
        this.url = target;
        this.headers = headers;
        this.verbose = verbose;
        this.outputFilename = outputFilename;
        this.data = data;
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

    // TODO: establish handshake
    // TODO: create packet
    // TODO: set payload of packet to request (bytes)
    protected final String connect() throws Exception {
        final int port = url.getPort();
        socket = new Socket(url.getHost(), port != -1 ? port : DEFAULT_PORT);
        out = socket.getOutputStream();
        in = socket.getInputStream();
        verboseContainer = new StringBuilder();
        outFmt = new Formatter(verboseContainer);

        setRequestHeaders();
        if (this instanceof PostRequest) {
            outFmt.format("Content-Length: %d\r%n", data == null ? 0 : data.length);
            outFmt.format("Content-Type: text/plain\r%n");
        }

        final String sent = verboseContainer.toString();
        out.write(sent.getBytes());
        out.write("\r\n".getBytes());
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                out.write((char) (data[i] & 0xFF));
            }
        }

        final String received = readData();
        close();

        if (verbose) {
            System.out.println(sent);
            System.out.println(received);
        }

        if (received.contains("HTTP/1.0 30") || received.contains("HTTP/1.1 30")) {
            url = new URL(getURL(received));
            return connect();
        }

        if (outputFilename != null) {
            writeToFile(sent, received);
        }

        return received;
    }

    private String readData() throws IOException {
        int character;
        final StringBuilder container = new StringBuilder();

        while ((character = in.read()) != -1) {
            container.append((char) character);
        }
        return container.toString();
    }

    private void close() throws IOException {
        outFmt.close();
        verboseContainer.setLength(0);
        in.close();
        out.close();
        socket.close();
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
