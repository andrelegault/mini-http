package com.comp445.httpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class HttpcTest {
    @Test
    public void testHttpcGet() {
        final String[] args = { "get", "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        assertEquals(test.action, "get");
        assertEquals("http://httpbin.org/get?course=networking&assignment=1", test.target);
    }

    @Test
    public void testHttpcGetVerbose() {
        final String[] args = { "get", "-v", "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertEquals(true, test.verbose);
    }

    @Test
    public void testHttpcGetWithSingleHeader() {
        final String[] args = { "get", "-v", "-h", "key:val", "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertEquals(true, test.verbose);
        assertEquals(1, test.headers.size());
        assertEquals("val", test.headers.get("key"));
    }

    @Test
    public void testHttpcGetWithTwoHeaders() {
        final String[] args = { "get", "-v", "-h", "key:val", "-h", "anotherKey:anotherVal",
                "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertEquals(true, test.verbose);
        assertEquals(2, test.headers.size());
        assertEquals("val", test.headers.get("key"));
        assertEquals("anotherVal", test.headers.get("anotherKey"));
    }

    @Test
    public void testHttpcGetWithData() {
        final String[] args = { "get", "-v", "-d", "'{ \"Assignment\": 1'",
                "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        assertEquals("get", test.action);
        assertEquals(true, test.verbose);
        assertEquals("anotherVal", test.data);
    }

    @Test
    public void testHttpcGetWithInvalidData() {
        final String[] args = { "get", "-v", "-d", "'{ \"Assignment\": 1'", "-f", "test.txt",
                "http://httpbin.org/get?course=networking&assignment=1" };
        assertThrows(Error.class, () -> new Httpc(args));
    }
}
