package org.myspringframework.web.http;

public class ResponseEntity<T> extends HttpEntity<T> {
    private final HttpStatusCode status;

    public ResponseEntity(T body, HttpStatusCode status) {
        super(body);
        this.status = status;
    }

    public ResponseEntity(HttpStatusCode status) {
        super(null);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatusCode.OK);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, HttpStatusCode.CREATED);
    }

    public static <T> ResponseEntity<T> unauthorized() {
        return new ResponseEntity<>(null, HttpStatusCode.UNAUTHORIZED);
    }

    public static <T> ResponseEntity<T> forbidden() {
        return new ResponseEntity<>(null, HttpStatusCode.FORBIDDEN);
    }

    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(null, HttpStatusCode.NOT_FOUND);
    }

    public static <T> ResponseEntity<T> badRequest() {
        return new ResponseEntity<>(null, HttpStatusCode.BAD_REQUEST);
    }

    public static <T> ResponseEntity<T> internalServerError() {
        return new ResponseEntity<>(null, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
}
