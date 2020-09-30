package com.comp445.httpc;

import java.util.Map;

public class HttpcPost extends HttpcRequest {
    private String data;

    public HttpcPost(final String host, Map<String, String> headers, final String data, final boolean verbose) {
        super(host, headers, verbose);
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
        out.printf("Content-Length: %d\r%n", getContentLength());
        out.printf("Content-Type: text/plain\r%n", getContentLength());
        out.printf("Accept-Encoding: gzip, deflate, br\r%n");
        out.printf("\r%n");
        if (data != null) {
            out.printf("%s\r%n", data);
        }
    }

}
