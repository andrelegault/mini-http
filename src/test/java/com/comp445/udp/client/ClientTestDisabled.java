package com.comp445.udp.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ClientTestDisabled {
    @Test
    public void testClientGetBadOrder() {
        final String[] args = { "get", "http://httpbin.org/get?course=networking&assignment=1", "-v" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientGet() {
        final String[] args = { "get", "-o", "outputFile.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals(test.action, "get");
        assertEquals("outputFile.txt", test.outputFilename);
        assertEquals("httpbin.org", test.target.getHost());
        assertEquals("/get", test.target.getPath());
        assertEquals("course=networking&assignment=1", test.target.getQuery());
    }

    @Test
    public void testClientGetVerbose() {
        final String[] args = { "get", "-v", "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("get", test.action);
        assertTrue(test.verbose);
    }

    @Test
    public void testClientGetWithSingleHeader() {
        final String[] args = { "get", "-v", "-h", "key:val", "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("get", test.action);
        assertTrue(test.verbose);
        assertEquals(1, test.headers.size());
        assertEquals("val", test.headers.get("key"));
    }

    @Test
    public void testClientGetSingleHeaderWithPort() {
        final String[] args = { "get", "-v", "-h", "key:val", "http://localhost:8081" };
        Client test = new Client(args);
        assertEquals("get", test.action);
        assertEquals("localhost", test.target.getHost());
        assertEquals(8081, test.target.getPort());
        assertTrue(test.verbose);
        assertEquals(1, test.headers.size());
        assertEquals("val", test.headers.get("key"));
    }

    @Test
    public void testClientGetWithUndefinedHeader() {
        final String[] args = { "get", "-v", "-h", "key:val", "-h", "-h",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientGetWithTwoHeaders() {
        final String[] args = { "get", "-v", "-h", "key:val", "-h", "anotherKey:anotherVal",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("get", test.action);
        assertTrue(test.verbose);
        assertEquals(2, test.headers.size());
        assertEquals("val", test.headers.get("key"));
        assertEquals("anotherVal", test.headers.get("anotherKey"));
    }

    @Test
    public void testClientGetWithInvalidHeader() {
        final String[] args = { "get", "-v", "-h", "get", "-h", "anotherKey:anotherVal",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientGetWithDataString() {
        final String[] args = { "get", "-v", "-d", "'{ \"Assignment\": 1 }'",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientPostWithInvalidDataFilename() {
        final String[] args = { "post", "-v", "-f", "idontexist.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientGetWithDataFilename() {
        final String[] args = { "get", "-v", "-f", "inputFile.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientGetInvalidHeader() {
        final String[] args = { "get", "-h", "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals(null, test.target);
    }

    @Test
    public void testClientPostWithData() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("post", test.action);
        assertTrue(test.verbose);
    }

    @Test
    public void testClientPostWithInvalidData() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'", "-f", "inputFile.txt",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertNull(test.req);
    }

    @Test
    public void testClientPostWithValidFileData() {
        final String[] args = { "post", "-v", "-f", "inputFile.txt",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("1234test\n".getBytes(), test.data);
    }

    @Test
    public void testClientPostWithValidStringData() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("'{ \"Assignment\": 1 }'".getBytes(), test.data);
    }

    @Test
    public void testClientGetOutputToFile() {
        final String[] args = { "get", "-v", "-o", "outputFile.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("outputFile.txt", test.outputFilename);
    }

    @Test
    public void testClientPostOutputToFile() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'", "-o", "outputFile.txt",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("'{ \"Assignment\": 1 }'".getBytes(), test.data);
        assertEquals("outputFile.txt", test.outputFilename);
    }

    @Test
    public void testClientPostNoBody() {
        final String[] args = { "post", "-v", "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals("httpbin.org", test.target.getHost());
        assertEquals(null, test.data);
    }

    @Test
    public void testClientPostInvalidHeader() {
        final String[] args = { "post", "-h", "http://httpbin.org/post?course=networking&assignment=1" };
        Client test = new Client(args);
        assertEquals(null, test.target);
        assertEquals(null, test.data);
    }
}
