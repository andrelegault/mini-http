package com.comp445.udp;

import java.net.InetSocketAddress;

public class Router {
    private static final int PORT = 3000;
    // private static final String HOSTNAME = "192.168.2.10";
    private static final String HOST = "localhost";

    public static final InetSocketAddress ADDRESS = new InetSocketAddress(HOST, PORT);
}
