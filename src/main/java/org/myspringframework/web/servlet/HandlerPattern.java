package org.myspringframework.web.servlet;

import org.myspringframework.web.bind.annotation.RequestMethod;

import java.util.Objects;
import java.util.regex.Pattern;

public final class HandlerPattern {
    private final RequestMethod httpMethod;
    private final Pattern regex;

    public HandlerPattern(
            RequestMethod httpMethod,
            Pattern regex
    ) {
        this.httpMethod = httpMethod;
        this.regex = regex;
    }

    public RequestMethod getHttpMethod() {
        return httpMethod;
    }

    public Pattern getRegex() {
        return regex;
    }

    public boolean matches(RequestMethod httpMethod, String path) {
        return this.httpMethod == httpMethod && this.regex.matcher(path).matches();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HandlerPattern) obj;
        return Objects.equals(this.httpMethod, that.httpMethod) &&
                Objects.equals(this.regex, that.regex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, regex);
    }

    @Override
    public String toString() {
        return "HandlerPattern[" +
                "httpMethod=" + httpMethod + ", " +
                "regex=" + regex + ']';
    }

}
