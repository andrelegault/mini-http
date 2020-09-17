package com.comp445.httpc;

import java.net.Socket;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

public abstract class HttpcRequest {
    public URL url;
    public Host host;
    public Map<String, Object> headers;

    protected HttpcRequest(URL url, Host host, Map<String, Object> headers) {
        this.url = url;
        this.host = host;
        this.headers = headers;
    }

    public abstract void connect();

    public String usageGeneral() {
        return "Usage:\n"
                .concat("   httpc command [arguments]\n")
                .concat("The commands are:\n")
                .concat("   get     executes a HTTP GET request and prints the response.\n")
                .concat("   post    executes a HTTP POST request and prints the response.\n")
                .concat("   help    prints this screen.\n")
                .concat("\nUse \"httpc help [command]\" for more information about a command.");
    }

    public String usageGet() {
        return "httpc help get\n"
                .concat("   usage: httpc get [-v] [-h key:value] URL\n")
                .concat("Get executes a HTTP GET request for a given URL.\n")
                .concat("   -v      Prints the detail of the response such as protocol, status, and headers.\n")
                .concat("   -h key:value    Associates headers to HTTP Request with the format 'key:value'.");
    }

    public String usagePost() {
        return "httpc help post\n".concat("    usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n")
                .concat("Post executes a HTTP POST request for a given URL with inline data or from file.\n")
                .concat("   get     executes a HTTP GET request and prints the response.\n")
                .concat("   post    executes a HTTP POST request and prints the response.\n")
                .concat("   help    prints this screen.\n")
                .concat("Use \"httpc help [command]\" for more information about a command.");
    }

}
