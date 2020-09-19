package com.comp445.httpc;

import java.io.IOException;
import java.net.URL;

public class Host {
    protected URL url;

    public Host(String endpoint) {
        try {
        url = new URL(endpoint);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}