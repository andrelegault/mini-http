package com.comp445.httpc;

import java.util.Map;

public class HttpcGet extends HttpcRequest {

    public HttpcGet(final Host host, final Map<String, String> headers) {
        super(host, headers);
    }

    @Override
    protected String getRequestType() {
        return "GET";
    }

    protected void setDataHeaders() {
    };

}
