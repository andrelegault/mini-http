package com.comp445.httpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    String dataFromFile;

    final String validDataString = "{ 'Assignment': 1, 'Thing': 2, 'OtherThing': 'Something' }";

    @BeforeAll
    public void testBeforeAll() {
        try {
            dataFromFile = Files.readString(Path.of("test.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        invalidHeaders.put("key1", "");
        invalidHeaders.put("", "val3");
        validHeaders.put("key1", "val1");
        validHeaders.put("key2", "val2");
    }

    @Test
    public void testHttpcRequestGetWithNoSlash() {
        final HttpcGet testGet = new HttpcGet("https://www.google.com", null, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestGet() {
        final HttpcGet testGet = new HttpcGet("http://httpbin.org/get?course=networking&assignment=1", null, verbose,
                null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcGetEmptyHeaders() {
        final HttpcGet testGet = new HttpcGet("http://httpbin.org/get?course=networking&assignment=1", emptyHeaders,
                verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcGetValidHeaders() {
        final HttpcGet testGet = new HttpcGet("http://httpbin.org/get?course=networking&assignment=1", validHeaders,
                verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testPost() {
        final HttpcPost testPost = new HttpcPost("http://httpbin.org/post", null, null, verbose, null);
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testPostWithData() {
        final HttpcPost testPost = new HttpcPost("http://httpbin.org/post", null, dataFromFile, verbose,
                "outputFile.txt");
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testPostWithDataString() {
        final HttpcPost testPost = new HttpcPost("http://httpbin.org/post", null, validDataString, verbose,
                "outputFile.txt");
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRedirect() {
        final HttpcGet testRedirect = new HttpcGet("http://google.com/", null, verbose,
                null);
        final String res = testRedirect.connect();
        assert (res.contains("200 OK"));
    }
}
