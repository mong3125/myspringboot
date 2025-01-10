package org.myspringframework.web.servlet;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HandlerMethod {
    private final Object controller;
    private final Method targetMethod;
    private final Pattern regex;
    private final List<String> variableNames;

    public HandlerMethod(
            Object controller,
            Method method,
            Pattern regex, List<String> variableNames) {
        this.controller = controller;
        this.targetMethod = method;
        this.regex = regex;
        this.variableNames = variableNames;
    }

    public Object getController() {
        return controller;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public Map<String, String> getPathVariables(String url) {
        Matcher matcher = regex.matcher(url);
        Map<String, String> variables = new HashMap<>();
        if (matcher.matches()) {
            for (int i = 0; i < variableNames.size(); i++) {
                variables.put(variableNames.get(i), matcher.group(i + 1));
            }
        }
        return variables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HandlerMethod) obj;
        return Objects.equals(this.controller, that.controller) &&
                Objects.equals(this.targetMethod, that.targetMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controller, targetMethod);
    }

    @Override
    public String toString() {
        return "HandlerMethod[" +
                "controller=" + controller + ", " +
                "method=" + targetMethod + ']';
    }
}
