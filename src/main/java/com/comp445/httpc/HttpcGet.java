package com.comp445.httpc;

import java.net.MalformedURLException;
import java.util.Map;

public class HttpcGet extends HttpcRequest {
    public HttpcGet(final String host, final Map<String, String> headers, final boolean verbose,
            final String outputFilename) throws MalformedURLException {
        super(host, headers, verbose, outputFilename);
    }

    @Override
    protected String getMethod() {
        return "GET";
    }

    protected void setDataHeaders() {
    };

}
