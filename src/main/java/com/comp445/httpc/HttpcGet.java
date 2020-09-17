package com.comp445.httpc;

import java.net.URL;
import java.util.Map;

public class HttpcGet extends HttpcRequest {

    public HttpcGet(URL url, Host host, Map<String, Object> headers) {
        super(url, host, headers);
    }

    @Override
    public void connect() {
        // TODO Auto-generated method stub
    }

}
