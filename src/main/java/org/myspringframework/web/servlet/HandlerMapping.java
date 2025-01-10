package org.myspringframework.web.servlet;

import org.myspringframework.annotations.Controller;
import org.myspringframework.context.ApplicationContext;
import org.myspringframework.web.bind.annotation.RequestMapping;
import org.myspringframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandlerMapping {
    private Map<HandlerPattern, HandlerMethod> handlerMap;
    private List<ExceptionHandlerMethod> exceptionHandlerMethods;

    public HandlerMapping(ApplicationContext context) {
        handlerMap = new ConcurrentHashMap<>();
        init(context);
    }

//    private void init(ApplicationContext context) {
//        for (Map.Entry<Class<?>, Object> entry : context.getBeanRegistry().entrySet()) {
//            // interface class
//            Class<?> clazz = entry.getKey();
//
//            // proxy object
//            Object bean = entry.getValue();
//
//            // target class
//            Class<?> targetClass = getTargetClass(bean);
//
//            if (isController(targetClass)) {
//                Object controller = context.getBean(clazz);
//
//                // controller base url
//                String baseUrl = getBaseUrl(targetClass);
//
//                for (Method targetMethod : targetClass.getMethods()) {
//                    if (targetMethod.isAnnotationPresent(RequestMapping.class)) {
//                        RequestMapping requestMapping = targetMethod.getDeclaredAnnotation(RequestMapping.class);
//                        Pattern regex = createRegex(baseUrl + requestMapping.value());
//                        List<String> variableNames = extractVariableNames(baseUrl + requestMapping.value());
//
//                        Method interfaceMethod = findInterfaceMethod(clazz, targetMethod);
//                        if (interfaceMethod == null) {
//                            throw new RuntimeException("Cannot find interface method for " + targetMethod);
//                        }
//
//                        HandlerPattern pattern = new HandlerPattern(requestMapping.method(), regex);
//                        HandlerMethod handler = new HandlerMethod(controller, interfaceMethod, targetMethod, regex, variableNames);
//                        handlerMap.put(pattern, handler);
//                    } else {
//                        for (Annotation annotation : targetMethod.getDeclaredAnnotations()) {
//                            if (annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
//                                String value = "";
//                                try {
//                                    value = annotation.getClass().getMethod("value").invoke(annotation).toString();
//                                } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
//                                    throw new RuntimeException("Failed to get value from annotation", e);
//                                }
//
//                                RequestMapping requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
//                                Pattern regex = createRegex(baseUrl + value);
//                                List<String> variableNames = extractVariableNames(baseUrl + value);
//
//                                Method interfaceMethod = findInterfaceMethod(clazz, targetMethod);
//                                if (interfaceMethod == null) {
//                                    throw new RuntimeException("Cannot find interface method for " + targetMethod);
//                                }
//
//                                HandlerPattern pattern = new HandlerPattern(requestMapping.method(), regex);
//                                HandlerMethod handler = new HandlerMethod(controller, interfaceMethod, targetMethod, regex, variableNames);
//                                handlerMap.put(pattern, handler);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    private void init(ApplicationContext context) {
        for (Map.Entry<Class<?>, Object> entry : context.getBeanRegistry().entrySet()) {
            // class type
            Class<?> clazz = entry.getKey();

            if (isController(clazz)) {
                Object controller = context.getBean(clazz);

                // controller base url
                String baseUrl = getBaseUrl(clazz);

                for (Method targetMethod : clazz.getMethods()) {
                    if (targetMethod.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = targetMethod.getDeclaredAnnotation(RequestMapping.class);
                        Pattern regex = createRegex(baseUrl + requestMapping.value());
                        List<String> variableNames = extractVariableNames(baseUrl + requestMapping.value());

                        HandlerPattern pattern = new HandlerPattern(requestMapping.method(), regex);
                        HandlerMethod handler = new HandlerMethod(controller, targetMethod, regex, variableNames);
                        handlerMap.put(pattern, handler);
                    } else {
                        for (Annotation annotation : targetMethod.getDeclaredAnnotations()) {
                            if (annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
                                String value = "";
                                try {
                                    value = annotation.getClass().getMethod("value").invoke(annotation).toString();
                                } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                                    throw new RuntimeException("Failed to get value from annotation", e);
                                }

                                RequestMapping requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
                                Pattern regex = createRegex(baseUrl + value);
                                List<String> variableNames = extractVariableNames(baseUrl + value);

                                HandlerPattern pattern = new HandlerPattern(requestMapping.method(), regex);
                                HandlerMethod handler = new HandlerMethod(controller, targetMethod, regex, variableNames);
                                handlerMap.put(pattern, handler);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getBaseUrl(Class<?> targetClass) {
        String baseUrl = "";
        if (targetClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = targetClass.getDeclaredAnnotation(RequestMapping.class);
            baseUrl = requestMapping.value();
        }
        return baseUrl;
    }

    private Method findInterfaceMethod(Class<?> interfaceClass, Method method) {
        for (Method interfaceMethod : interfaceClass.getMethods()) {
            if (interfaceMethod.getName().equals(method.getName()) && Arrays.equals(interfaceMethod.getParameterTypes(), method.getParameterTypes())) {
                return interfaceMethod;
            }
        }
        return null;
    }

    private Pattern createRegex(String template) {
        String regexStr = template.replaceAll("\\{([^/}]+)\\}", "([^/]+)");
        return Pattern.compile("^" + regexStr + "$");
    }

    private List<String> extractVariableNames(String template) {
        List<String> variableNames = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([^/}]+)\\}").matcher(template);
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }
        return variableNames;
    }

    private boolean isController(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Controller.class)) {
            return true;
        }

        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Controller.class)) {
                return true;
            }
        }

        return false;
    }

    public HandlerMethod getHandler(RequestMethod httpMethod, String path) {
        for (Map.Entry<HandlerPattern, HandlerMethod> entry : handlerMap.entrySet()) {
            HandlerPattern pattern = entry.getKey();
            if (pattern.matches(httpMethod, path)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public HandlerMethod getHandler(String httpMethod, String path) {
        return getHandler(RequestMethod.valueOf(httpMethod.toUpperCase()), path);
    }
}
