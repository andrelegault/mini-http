package com.comp445;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private static final String[] inlineExts = { "jpg", "png", "html", "htm", "txt" };

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

    // Status code of this response.
    private final int statusCode;

    private final Path path;

    // Response body.
    public final byte[] body;

    // Request line
    public final String statusLine;

    public final Map<String, String> headers = new HashMap<String, String>();

    /**
     * Constructor.
     * 
     * @param statusCode Status code for the response.
     * @throws IOException
     */
    public HttpcResponse(final int statusCode) throws IOException {
        this.statusCode = statusCode;
        this.body = null;
        this.path = null;

        this.statusLine = this.getRequestLine();
        this.setHeaders();
    }

    /**
     * Constructor.
     * 
     * @param statusCode Status code for the response.
     * @throws IOException
     */
    public HttpcResponse(final int statusCode, final Path path, final byte[] body) throws IOException {
        this.statusCode = statusCode;
        this.body = body;
        this.path = path;

        this.statusLine = this.getRequestLine();
        this.setHeaders();
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

    private void setHeaders() throws IOException {
        headers.put("Date: ", getDate());
        headers.put("Connection", "Close");
        if (body != null) {
            final String predictedContentType = predictContentType();
            final String predictedContentDisPosition = predictContentDisposition();
            headers.put("Content-Type", predictedContentType == null ? "text/plain" : predictedContentType);
            headers.put("Content-Disposition", predictedContentDisPosition);
            headers.put("Content-Length", Integer.toString(body.length));
        }
    }

    private String predictContentDisposition() {
        final String ext = extractExtension(this.path.toString());
        // try to display these filetypes
        final boolean isInline = ext == null || Arrays.stream(HttpcResponse.inlineExts).anyMatch(ext::equals);
        return isInline ? "inline" : "attachment";
    }

    private String extractExtension(String string) {
        final int lastIndexOfDot = string.lastIndexOf(".");
        string = lastIndexOfDot == -1 || lastIndexOfDot == (string.length() - 1) ? null
                : string.substring(lastIndexOfDot + 1);
        return string;
    }

    private String predictContentType() throws IOException {
        return Files.probeContentType(this.path);
    }

    private String getRequestLine() {
        return HttpcResponse.PROTOCOL + "/" + HttpcResponse.VERSION + " " + this.statusCode + " "
                + getStatusCodeResponse(this.statusCode);
    }

    /**
     * To represent this object.
     * 
     * @return String representation of this object.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.statusLine);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append("\r\n" + entry.getKey() + ": " + entry.getValue());
        }
        if (body != null) {
            builder.append("\r\n\r\n");
            // builder.append(new String(body, StandardCharsets.UTF_8));
            // builder.append(body);
        }
        return builder.toString();
    }

    private String getDate() {
        return HttpcResponse.dateFormat.format(new Date());
    }
}
