package org.myspringframework.web.http;

public enum HttpStatusCode {
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500);

    private final int value;

    HttpStatusCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
