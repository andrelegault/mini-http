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
    protected final int HTTP_PORT = 80;

    protected HttpcRequest(final Host host, final Map<String, String> headers) {
        this.host = host;
        this.headers = headers;
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
            if (Httpc.isVerbose) {
                System.out.println(data);
            }
            close();
            return data;
        } catch (final MalformedURLException e) {
            System.out.println(e.getMessage());
        } catch (final UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (final IOException e) {
            System.out.println(e.getMessage());
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

    protected String getQueryIfPresent() {
        return (!host.url.getQuery().equals("") && !host.url.getQuery().equals("/")) ? "?" + host.url.getQuery() : "";
    }

    protected void setRequestHeaders() {
        out.printf("%s %s%s  HTTP/1.1\r%n", getMethod(), host.url.getPath(), getQueryIfPresent());
        out.printf("Host: " + host.url.getHost() + "\r%n");
        out.printf("Upgrade-Insecure-Requests: 1\r%n");
        out.printf("Connection: Close\r%n");
        out.printf("DNT: 1\r%n");
    }

}
