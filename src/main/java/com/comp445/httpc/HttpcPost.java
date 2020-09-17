package com.comp445.httpc;

import java.net.URL;
import java.util.Map;

public class HttpcPost extends HttpcRequest {

    public HttpcPost(URL url, Host host, Map<String, Object> headers) {
        super(url, host, headers);
    }

    @Override
    public void connect() {
        // TODO Auto-generated method stub
    }

}
