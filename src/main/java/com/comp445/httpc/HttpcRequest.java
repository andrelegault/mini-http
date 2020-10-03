package com.comp445.httpc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class HttpcRequest {
    /**
     * This class represents a request made by httpc. Only GET/POST requests are to
     * be supported.
     * 
     * Methods implemented follow https://tools.ietf.org/html/rfc7230#section-3.2
     */
    protected PrintWriter out;
    private Host host;
    private Map<String, String> headers;
    private Socket socket;
    private BufferedReader in;
    private final boolean verbose;
    private final String outputFilename;

    private final int HTTP_PORT = 80;
    private StringBuilder verboseContainer;
    protected Formatter outFmt;

    protected HttpcRequest(final String hostString, final Map<String, String> headers, final boolean verbose,
            final String outputFilename) {
        this.host = new Host(hostString);
        this.headers = headers;
        this.verbose = verbose;
        this.outputFilename = outputFilename;
    }

    protected abstract void setDataHeaders();

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

    protected String connect() {
        try {
            socket = new Socket(host.url.getHost(), HTTP_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            verboseContainer = new StringBuilder();
            outFmt = new Formatter(verboseContainer);

            setRequestHeaders();
            setDataHeaders();
            outFmt.format("\r%n");

            final String sent = verboseContainer.toString();
            out.println(sent);

            String received = readData();
            close();

            if (verbose) {
                System.out.println(sent);
                System.out.println(received);
            }

            if (outputFilename != null) {
                writeToFile(sent, received);
            }

            if(received.contains("HTTP/1.0 30") || received.contains("HTTP/1.1 30")){
              host.url = new URL(getURL(received));
              return connect();
            }

            return received;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return "An error occurred!";
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
        final String query = host.url.getQuery();
        return (query != null && !query.equals("") && !query.equals("/")) ? "?" + host.url.getQuery() : "";
    }

    private String sanitizePath(String unsanitizedPath) {
        return unsanitizedPath.equals("") ? "/" : unsanitizedPath;
    }

    private void setRequestHeaders() {
        outFmt.format("%s %s%s HTTP/1.0\r%n", getMethod(), sanitizePath(host.url.getPath()), getQueryOrEmptyString());
        outFmt.format("Host: %s\r%n", host.url.getHost());
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

    private String splitLines(String response){
        String[] lines = response.split(System.getProperty("line.separator"));
        for(int i=0; i<lines.length; i++){
            if(lines[i].startsWith("HTTP") || lines[i].startsWith("Location") || lines[i].startsWith("<") || lines[i].isBlank() || lines[i].startsWith("The") ){
                lines[i]="";
            }
        }
        StringBuilder finalStringBuilder= new StringBuilder("");
        for(String s:lines){
            if(!s.equals("")){
                finalStringBuilder.append(s).append(System.getProperty("line.separator"));
            }
        }
        return finalStringBuilder.toString();
    }

    private Map<String, String> convertWithStream(String mapAsString) {
        return Arrays.stream(mapAsString.split("\\r?\\n"))
                .map(entry -> entry.split(":"))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
    }

    private String getURL(String response){
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
