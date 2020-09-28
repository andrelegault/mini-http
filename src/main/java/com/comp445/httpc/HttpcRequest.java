package com.comp445.httpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

public abstract class HttpcRequest {
    /**
     * This class represents a request made by httpc. Only GET/POST requests are to
     * be supported.
     * 
     * Methods implemented follow https://tools.ietf.org/html/rfc7230#section-3.2
     */
    protected Host host;
    protected Map<String, String> headers;
    protected Socket socket;
    protected BufferedReader in;
    protected PrintWriter out;
    protected boolean verbose;
    protected final int HTTP_PORT = 80;

    protected HttpcRequest(final String hostString, final Map<String, String> headers, final boolean verbose) {
        this.host = new Host(hostString);
        this.headers = headers;
        this.verbose = verbose;
    }

    protected abstract void setDataHeaders();

    protected abstract String getMethod();

    protected String connect() {
        try {
            socket = new Socket(host.url.getHost(), HTTP_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            setRequestHeaders();
            setDataHeaders();
            out.printf("\r%n");

            final String data = readData();
            if (verbose) {
                System.out.println(data);
            }
            close();
            return data;
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return "An error occurred!";
    }

    protected String readData() throws IOException {
        int character;
        final StringBuilder container = new StringBuilder();

        while ((character = in.read()) != -1) {
            container.append((char) character);
        }
        return container.toString();
    }

    private void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    protected String getQueryOrEmptyString() {
        final String query = host.url.getQuery();
        return (query != null && !query.equals("") && !query.equals("/")) ? "?" + host.url.getQuery() : "";
    }

    protected void setRequestHeaders() {
        out.printf("%s %s%s  HTTP/1.1\r%n", getMethod(), host.url.getPath(), getQueryOrEmptyString());
        out.printf("Host: " + host.url.getHost() + "\r%n");
        out.printf("Upgrade-Insecure-Requests: 1\r%n");
        out.printf("Connection: Close\r%n");
        out.printf("DNT: 1\r%n");
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                out.printf("%s: %s", entry.getKey(), entry.getValue());
            }
        }
    }

}
