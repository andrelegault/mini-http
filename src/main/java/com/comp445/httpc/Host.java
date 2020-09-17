package com.comp445.httpc;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class Host {
    public Socket socket;
    private URL url;
    public OutputStream outputStream;
    public InputStream inputStream;

    public Host(String endpoint) {
        try {
            url = new URL(endpoint);
            socket = new Socket(url.getHost(), 80);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}