package com.comp445.udp.client;

import java.util.Map;
import java.io.IOException;
import java.net.URL;

public class GetRequest extends Request {
    public GetRequest(final URL host, final Map<String, String> headers, final boolean verbose,
            final String outputFilename) throws IOException {
        super(host, headers, verbose, outputFilename, null);
    }

    @Override
    protected String getMethod() {
        return "GET";
    }
}
