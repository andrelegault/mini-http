package com.comp445.httpc;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpcRequestTest {

    // @Test
    // public void postTest() {
    // Host testHost = new
    // Host("http://httpbin.org/get?course=networking&assignment=1");
    // HttpcPost testPost = new HttpcPost(testHost, null, null);
    // String res = testPost.connect();
    // System.out.println(res);
    // assertNotEquals(res, null);
    // }

    @Test
    public void httpcGetTest() {
        Host testHost = new Host("http://httpbin.org/get?course=networking&assignment=1");
        HttpcGet testGet = new HttpcGet(testHost, null);
        String res = testGet.connect();
        assertFalse("Bad request!", res.contains("400 Bad Request"));
    }
}
