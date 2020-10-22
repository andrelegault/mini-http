package com.comp445;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a response sent by an httpc server. The logic should be
 * on the side of the server. This class shouldn't mess with the filesystem, or
 * check permissions or anything like that.
 */
public class HttpcResponse {
    // Version of HTTP supported by this response.
    private static final String VERSION = "1.0";

    // Protocol supported by this response.
    private static final String PROTOCOL = "HTTP";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

    // Status code of this response.
    private final int statusCode;

    // Response body.
    public final String body;

    /**
     * Constructor.
     * 
     * @param statusCode Status code for the response.
     */
    public HttpcResponse(final int statusCode) {
        this.statusCode = statusCode;
        this.body = null;
    }

    /**
     * Constructor.
     * 
     * @param statusCode Status code for the response.
     */
    public HttpcResponse(final int statusCode, final String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    /**
     * Gets a response message, given a status code. source:
     * https://tools.ietf.org/html/rfc1945#page-26
     * 
     * We don't support 3XX status codes as we only host files. Apparently we only
     * have to support 5 codes (source: sectheta twitch livestream on oct. 20,
     * 2020). Pretty sure the codes to support are 200, 202, 400, 403, and 404.
     * 
     * @param statusCode Status code.
     * @return Message associated to the status code.
     */
    private static String getStatusCodeResponse(final int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 202:
                return "Accepted";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            default:
                return "Unrecognized status code";
        }
    }

    /**
     * To represent this object.
     * 
     * @return String representation of this object.
     */
    @Override
    public String toString() {
        String response = PROTOCOL + "/" + VERSION + " " + this.statusCode + " "
                + getStatusCodeResponse(this.statusCode);
        response += "\r\nDate: " + getDate();
        response += "\r\nConnection: Close";
        if (body != null) {
            response += "\r\nContent-Type: text/plain";
            response += "\r\nContent-Length: " + body.length();
            response += "\r\n\r\n" + body;
        }
        return response;
    }

    private String getDate() {
        return dateFormat.format(new Date());
    }
}
