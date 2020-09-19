package com.comp445.httpc;

import java.util.Map;

public class HttpcPost extends HttpcRequest {
    private String data;

    public HttpcPost(Host host, Map<String, String> headers, String data) {
        super(host, headers);
        this.data = data;
    }

    @Override
    protected String getRequestType() {
        return "POST";
    }

    protected void setDataHeaders() {
        if (headers != null) {
            out.print("data: " + data + "\r\n");
        }
    }

}
