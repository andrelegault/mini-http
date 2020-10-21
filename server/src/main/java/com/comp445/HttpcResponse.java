package com.comp445;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a response sent by an httpc server.
 */
public class HttpcResponse {
    // Version of HTTP supported by this response.
    private static final String VERSION = "1.0";

    // Protocol supported by this response.
    private static final String PROTOCOL = "HTTP";

    // Status code of this response.
    private final int statusCode;

    /**
     * Constructor.
     * 
     * @param statusCode Status code for the response.
     */
    public HttpcResponse(final int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns whether the provided http header is valid.
     * 
     * @param httpHeader First line of the http request.
     * @return true if the header is valid; false otherwise.
     */
    public static boolean isValid(final String httpHeader) {
        final Matcher matcher = pattern.matcher(httpHeader);
        return matcher.find();
    }

    /**
     * Gets a response message, given a status code. source:
     * https://tools.ietf.org/html/rfc1945#page-26
     * 
     * We don't support 3XX status codes as we only host files.
     * 
     * @param statusCode Status code.
     * @return Message associated to the status code.
     */
    private static String getStatusCodeResponse(final int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
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
    public String toString() {
        return PROTOCOL + "/" + VERSION + " " + this.statusCode + " " + getStatusCodeResponse(this.statusCode);
    }
}
