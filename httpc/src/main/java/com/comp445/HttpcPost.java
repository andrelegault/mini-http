package com.comp445;

import java.net.MalformedURLException;
import java.util.Map;
import java.net.URL;

public class HttpcPost extends HttpcRequest {
    public HttpcPost(final URL target, Map<String, String> headers, final byte[] data, final boolean verbose,
            final String outputFilename) throws MalformedURLException {
        super(target, headers, verbose, outputFilename, data);
    }

    @Override
    protected String getMethod() {
        return "POST";
    }
}
