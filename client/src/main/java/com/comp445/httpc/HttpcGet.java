package com.comp445.httpc;

import java.util.Map;
import java.net.URL;

public class HttpcGet extends HttpcRequest {
    public HttpcGet(final URL host, final Map<String, String> headers, final boolean verbose,
            final String outputFilename) {
        super(host, headers, verbose, outputFilename);
    }

    @Override
    protected String getMethod() {
        return "GET";
    }

    protected void setDataHeaders() {
    };

}
