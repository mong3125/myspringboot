package org.myspringframework.web.servlet;

import java.lang.reflect.Method;

public class ExceptionHandlerMethod {
    private final Object controller;
    private final Method method;
    private final Class<? extends Throwable>[] exceptionTypes;

    public ExceptionHandlerMethod(Object controller, Method method, Class<? extends Throwable>[] exceptionTypes) {
        this.controller = controller;
        this.method = method;
        this.exceptionTypes = exceptionTypes;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public Class<? extends Throwable>[] getExceptionTypes() {
        return exceptionTypes;
    }

    public boolean supports(Throwable exception) {
        for (Class<? extends Throwable> exType : exceptionTypes) {
            if (exType.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }
        return false;
    }
}
