package com.comp445;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpcRequestTest {
    final boolean verbose = false;

    final Map<String, String> invalidHeaders = new HashMap<String, String>();
    final Map<String, String> validHeaders = new HashMap<String, String>();
    final Map<String, String> emptyHeaders = new HashMap<String, String>();

    byte[] dataFromFile;
    URL get;
    URL getPort;
    URL getQuery;
    URL getPortQuery;
    URL post;
    URL postPort;
    URL postQuery;
    URL postPortQuery;

    final String validDataString = "{ 'Assignment': 1, 'Thing': 2, 'OtherThing': 'Something' }";

    @BeforeAll
    public void testBeforeAll() {
        try {
            dataFromFile = Files.readAllBytes(Path.of("inputFile.txt"));
            get = new URL("http://httpbin.org/get");
            getPort = new URL("http://httpbin.org:80/get");
            getQuery = new URL("http://httpbin.org/get?course=networking&assignment=1");
            getPortQuery = new URL("http://httpbin.org:80/get?course=networking&assignment=1");
            post = new URL("http://httpbin.org/post");
            postPort = new URL("http://httpbin.org:80/post");
            postQuery = new URL("http://httpbin.org/post?course=networking&assignment=1");
            postPortQuery = new URL("http://httpbin.org:80/post?course=networking&assignment=1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidHeaders.put("key1", "");
        invalidHeaders.put("", "val3");
        validHeaders.put("key1", "val1");
        validHeaders.put("key2", "val2");
    }

    @Test
    public void testHttpcRequestGet() throws Exception {
        final HttpcGet testGet = new HttpcGet(get, null, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestGetPort() throws Exception {
        final HttpcGet testGet = new HttpcGet(getPort, null, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestGetQuery() throws Exception {
        final HttpcGet testGet = new HttpcGet(getPort, null, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestGetPortQuery() throws Exception {
        final HttpcGet testGet = new HttpcGet(getPortQuery, null, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestPost() throws Exception {
        final HttpcPost testPost = new HttpcPost(post, null, null, verbose, null);
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestPostPort() throws Exception {
        final HttpcPost testPost = new HttpcPost(postPort, null, null, verbose, null);
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestPostQuery() throws Exception {
        final HttpcPost testPost = new HttpcPost(postPort, null, null, verbose, null);
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestPostPortQuery() throws Exception {
        final HttpcPost testPost = new HttpcPost(postPortQuery, null, null, verbose, null);
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRequestGetWithNoSlash() throws Exception {
        final HttpcGet testGet = new HttpcGet(new URL("https://www.google.com"), null, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcGetEmptyHeaders() throws Exception {
        final HttpcGet testGet = new HttpcGet(get, emptyHeaders, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcGetValidHeaders() throws Exception {
        final HttpcGet testGet = new HttpcGet(get, validHeaders, verbose, null);
        final String res = testGet.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testPost() throws Exception {
        final HttpcPost testPost = new HttpcPost(post, null, null, verbose, null);
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testPostWithData() throws Exception {
        final HttpcPost testPost = new HttpcPost(post, null, dataFromFile, verbose, "outputFile.txt");
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testPostWithDataString() throws Exception {
        final HttpcPost testPost = new HttpcPost(post, null, validDataString.getBytes(), verbose, "outputFile.txt");
        final String res = testPost.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRedirect() throws Exception {
        final HttpcGet testRedirect = new HttpcGet(new URL("https://www.google.com"), null, verbose, null);
        final String res = testRedirect.connect();
        assert (res.contains("200 OK"));
    }

    @Test
    public void testHttpcRedirectOutputToFile() throws Exception {
        final HttpcGet testRedirect = new HttpcGet(new URL("https://www.google.com"), null, verbose, "outputFile.txt");
        final String res = testRedirect.connect();
        assert (res.contains("200 OK"));
    }
}
