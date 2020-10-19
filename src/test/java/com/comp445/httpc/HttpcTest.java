package com.comp445.httpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HttpcTest {
    @Test
    public void testHttpcGetBadOrder() {
        final String[] args = { "get", "http://httpbin.org/get?course=networking&assignment=1", "-v" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcGet() {
        final String[] args = { "get", "-o", "outputFile.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals(test.action, "get");
        assertEquals("outputFile.txt", test.outputFilename);
        assertEquals("http://httpbin.org/get?course=networking&assignment=1", test.target);
    }

    @Test
    public void testHttpcGetVerbose() {
        final String[] args = { "get", "-v", "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertTrue(test.verbose);
    }

    @Test
    public void testHttpcGetWithSingleHeader() {
        final String[] args = { "get", "-v", "-h", "key:val", "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertTrue(test.verbose);
        assertEquals(1, test.headers.size());
        assertEquals("val", test.headers.get("key"));
    }

    @Test
    public void testHttpcGetWithUndefinedHeader() {
        final String[] args = { "get", "-v", "-h", "key:val", "-h", "-h",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcGetWithTwoHeaders() {
        final String[] args = { "get", "-v", "-h", "key:val", "-h", "anotherKey:anotherVal",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertTrue(test.verbose);
        assertEquals(2, test.headers.size());
        assertEquals("val", test.headers.get("key"));
        assertEquals("anotherVal", test.headers.get("anotherKey"));
    }

    @Test
    public void testHttpcGetWithInvalidHeader() {
        final String[] args = { "get", "-v", "-h", "get", "-h", "anotherKey:anotherVal",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcGetWithDataString() {
        final String[] args = { "get", "-v", "-d", "'{ \"Assignment\": 1 }'",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcPostWithInvalidDataFilename() {
        final String[] args = { "post", "-v", "-f", "idontexist.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcGetWithDataFilename() {
        final String[] args = { "get", "-v", "-f", "inputFile.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcGetInvalidHeader() {
        final String[] args = { "get", "-h", "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals(null, test.target);
    }

    @Test
    public void testHttpcPostWithData() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("post", test.action);
        assertTrue(test.verbose);
    }

    @Test
    public void testHttpcPostWithInvalidData() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'", "-f", "inputFile.txt",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertNull(test.req);
    }

    @Test
    public void testHttpcPostWithValidFileData() {
        final String[] args = { "post", "-v", "-f", "inputFile.txt",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("1234test\n", test.data);
    }

    @Test
    public void testHttpcPostWithValidStringData() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("'{ \"Assignment\": 1 }'", test.data);
    }

    @Test
    public void testHttpcGetOutputToFile() {
        final String[] args = { "get", "-v", "-o", "outputFile.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("outputFile.txt", test.outputFilename);
    }

    @Test
    public void testHttpcPostOutputToFile() {
        final String[] args = { "post", "-v", "-d", "'{ \"Assignment\": 1 }'", "-o", "outputFile.txt",
                "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("'{ \"Assignment\": 1 }'", test.data);
        assertEquals("outputFile.txt", test.outputFilename);
    }

    @Test
    public void testHttpcPostNoBody() {
        final String[] args = { "post", "-v", "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals("http://httpbin.org/post?course=networking&assignment=1", test.target);
        assertEquals(null, test.data);
    }

    @Test
    public void testHttpcPostInvalidHeader() {
        final String[] args = { "post", "-h", "http://httpbin.org/post?course=networking&assignment=1" };
        Httpc test = new Httpc(args);
        assertEquals(null, test.target);
        assertEquals(null, test.data);
    }
}
