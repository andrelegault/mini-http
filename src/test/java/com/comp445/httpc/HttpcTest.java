package com.comp445.httpc;

import org.junit.Test;

public class HttpcTest {
    @Test
    public void testHttpcGet() {
        final String[] args = {"get", "-v", "https://www.google.com"};
        final Httpc test = new Httpc(args);
        assert(test.req != null);
        test.req.connect();
    }
}
