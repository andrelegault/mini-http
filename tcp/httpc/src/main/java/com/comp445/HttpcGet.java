package com.comp445;

import java.util.Map;
import java.net.URL;

public class HttpcGet extends HttpcRequest {
    public HttpcGet(final URL host, final Map<String, String> headers, final boolean verbose,
            final String outputFilename) {
        super(host, headers, verbose, outputFilename, null);
    }

    @Override
    protected String getMethod() {
        return "GET";
    }
}
