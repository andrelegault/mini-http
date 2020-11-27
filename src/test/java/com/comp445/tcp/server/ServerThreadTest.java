package com.comp445.tcp.server;

import com.comp445.tcp.client.Client;

import org.junit.jupiter.api.Test;

public class ServerThreadTest {
    @Test
    public void testMultipleRequests() {
        final String[] args = { "post", "http://localhost:8080/data.txt" };
        for (int i = 0; i < 5; i++) {
            new Client(args);
        }
    }
}
