package com.interview.web.advice.exception.handling;

import java.net.URI;

public enum ProblemType {

    CLIENT_ERROR(URI.create("/tekmetric/problems/client-error")),
    SERVER_INTERNAL_ERROR(URI.create("/tekmetric/problems/internal-error"));

    private URI uri;

    ProblemType(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }
}
