package com.comp445.httpc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpcRequestTest {
    final boolean verbose = false;

    final Map<String, String> invalidHeaders = new HashMap<String, String>();
    final Map<String, String> validHeaders = new HashMap<String, String>();
    final Map<String, String> emptyHeaders = new HashMap<String, String>();

    final String validDataFilename = "test.txt";
    final String invalidDataFilename = "idontexist.txt";
    final String emptyDataFilename = "";

    final String validDataString = "{ 'Assignment': 1, 'Thing': 2, 'OtherThing': 'Something' }";
    final String invalidDataString = "thing";
    final String emptyDataString = "";

    @BeforeAll
    public void testBeforeAll() {
        invalidHeaders.put("key1", "");
        invalidHeaders.put("", "");
        invalidHeaders.put("", "val3");
        validHeaders.put("key1", "val1");
        validHeaders.put("key2", "val2");
    }

    @Test
    public void testHttpcRequestGet() {
        final HttpcGet testGet = new HttpcGet("http://httpbin.org/get?course=networking&assignment=1", null, verbose);
        final String res = testGet.connect();
        assert(res.contains("200 OK"));
    }

    @Test
    public void testHttpcGetEmptyHeaders() {
        final HttpcGet testGet = new HttpcGet("http://httpbin.org/get?course=networking&assignment=1", emptyHeaders,
                verbose);
        final String res = testGet.connect();
        assert(res.contains("200 OK"));
    }

    @Test
    public void testHttpcGetValidHeaders() {
        final HttpcGet testGet = new HttpcGet("http://httpbin.org/get?course=networking&assignment=1", validHeaders,
                verbose);
        final String res = testGet.connect();
        assert(res.contains("200 OK"));
    }

    @Test
    public void testPost() {
        final HttpcPost testPost = new HttpcPost("http://httpbin.org/get?course=networking&assignment=1", null, null,
                verbose);
        final String res = testPost.connect();
        assert(res.contains("200 OK"));
    }

    @Test
    public void testHttpcPostInvalidData() {
        final HttpcPost testPost = new HttpcPost("http://httpbin.org/get?course=networking&assignment=1", null,
                invalidDataFilename, verbose);
        final String res = testPost.connect();
        assert(res.contains("200 OK"));
    }
}
