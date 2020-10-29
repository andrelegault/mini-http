package com.comp445;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HttpcThreadTest {
    @Test
    public void testMultipleRequests() {
        final String[] args = { "get", "http://localhost:8080/data.txt" };
        for (int i = 0; i < 5; i++) {
            new Httpc(args);
        }
    }
}
