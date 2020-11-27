package com.comp445.udp.client;

import java.io.IOException;
import java.util.Map;
import java.net.URL;

public class PostRequest extends Request {
    public PostRequest(final URL target, Map<String, String> headers, final byte[] data, final boolean verbose,
            final String outputFilename) throws IOException {
        super(target, headers, verbose, outputFilename, data);
    }

    @Override
    protected String getMethod() {
        return "POST";
    }
}
