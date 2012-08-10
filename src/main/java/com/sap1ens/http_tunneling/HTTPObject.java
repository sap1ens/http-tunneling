package com.sap1ens.http_tunneling;

import java.util.LinkedHashMap;

/**
 * Store data related to Client/Server responses/requests
 *
 * @author sap1ens
 */
public class HTTPObject {

    private LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
    private StringBuilder content = new StringBuilder();
    private String uri;

    public LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(LinkedHashMap<String, String> headers) {
        this.headers = headers;
    }

    public StringBuilder getContent() {
        return content;
    }

    public void setContent(StringBuilder content) {
        this.content = content;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
