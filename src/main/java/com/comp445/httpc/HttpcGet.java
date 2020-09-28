package com.comp445.httpc;

import java.util.Map;

public class HttpcGet extends HttpcRequest {
    public HttpcGet(final String host, final Map<String, String> headers) {
        super(host, headers);
    }

    @Override
    protected String getMethod() {
        return "GET";
    }

    protected void setDataHeaders() {
    };

}
