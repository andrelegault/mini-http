package com.comp445;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class ServerTest {
    @Test
    public void testShouldAnswerWithTrue() {
        final String[] args = {"-v", "-p", "8081"};
        final Server server = new Server(args);
    }
}
