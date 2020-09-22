package com.comp445.httpc;

import java.util.Map;

public class HttpcPost extends HttpcRequest {
    private String data;

    public HttpcPost(Host host, Map<String, String> headers, String data) {
        super(host, headers);
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
        out.print("Content-Length: " + getContentLength() + "\r\n");
        if (headers != null) {
            out.print("data: " + data + "\r\n");
        }
    }

}
