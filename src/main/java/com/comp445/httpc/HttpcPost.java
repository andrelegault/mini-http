package com.comp445.httpc;

import java.net.MalformedURLException;
import java.util.Map;

public class HttpcPost extends HttpcRequest {
    private final String data;

    public HttpcPost(final String host, Map<String, String> headers, final String data, final boolean verbose,
            final String outputFilename) throws MalformedURLException {
        super(host, headers, verbose, outputFilename);
        this.data = data;
    }

    @Override
    protected String getMethod() {
        return "POST";
    }

    private int getContentLength() {
        return data != null ? data.length() : 0;
    }

    protected void setDataHeaders() {
        outFmt.format("Content-Length: %d\r%n", getContentLength());
        outFmt.format("Content-Type: text/plain\r%n");
        outFmt.format("\r%n");
        if (data != null) {
            outFmt.format("%s\r%n", data);
        }
    }

}
