package com.comp445.httpc;

import org.junit.Test;
import org.junit.Assert;

public class HttpcTest {
    @Test
    public void testHttpcGet() {
        final String[] args = { "get", "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        Assert.assertEquals(test.action, "get");
        Assert.assertEquals("http://httpbin.org/get?course=networking&assignment=1", test.target);
    }

    @Test
    public void testHttpcGetVerbose() {
        final String[] args = { "get", "-v", "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        Assert.assertEquals("get", test.action);
        Assert.assertEquals(true, test.verbose);
    }

    @Test
    public void testHttpcGetWithSingleHeader() {
        final String[] args = { "get", "-v", "-h", "key:val", "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        Assert.assertEquals("get",test.action);
        Assert.assertEquals(true, test.verbose);
        Assert.assertEquals(1, test.headers.size());
        Assert.assertEquals("val", test.headers.get("key"));
    }

    @Test
    public void testHttpcGetWithTwoHeaders() {
        final String[] args = { "get", "-v", "-h", "key:val", "-h", "anotherKey:anotherVal",
                "http://httpbin.org/get?course=networking&assignment=1" };
        final Httpc test = new Httpc(args);
        Assert.assertEquals("get",test.action);
        Assert.assertEquals(true, test.verbose);
        Assert.assertEquals(2, test.headers.size());
        Assert.assertEquals("val", test.headers.get("key"));
        Assert.assertEquals("anotherVal", test.headers.get("anotherKey"));
    }
}
