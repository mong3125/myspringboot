package org.myspringframework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myspringframework.annotations.Autowired;
import org.myspringframework.mapper.ObjectMapper;
import org.myspringframework.web.bind.annotation.PathVariable;
import org.myspringframework.web.bind.annotation.RequestBody;
import org.myspringframework.web.bind.annotation.RequestParam;
import org.myspringframework.web.http.ErrorResponse;
import org.myspringframework.web.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerAdapter {
    @Autowired
    private ObjectMapper objectMapper;

    public void handle(HttpServletRequest req, HttpServletResponse resp, HandlerMethod handler) throws IOException {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());
        String queryString = req.getQueryString();
        String requestBody = getRequestBody(req);

        Map<String, String> pathVariables = handler.getPathVariables(path);
        Map<String, String> queryParams = parseQueryString(queryString);

        Method method = handler.getTargetMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            try {
                if (parameter.isAnnotationPresent(PathVariable.class)) {
                    PathVariable pathVariable = parameter.getDeclaredAnnotation(PathVariable.class);
                    String parameterName = pathVariable.value();
                    Object value = objectMapper.readValue(pathVariables.get(parameterName), parameter.getType());
                    args[i] = value;
                } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                    args[i] = objectMapper.readValue(requestBody, parameter.getType());
                } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    RequestParam requestParam = parameter.getDeclaredAnnotation(RequestParam.class);
                    String parameterName = requestParam.value();
                    Object value = objectMapper.readValue(queryParams.get(parameterName), parameter.getType());
                    args[i] = value;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to bind request parameter to method parameter", e);
            }
        }

        try {
            Object result = method.invoke(handler.getController(), args);

            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                response(resp, responseEntity.getStatus().value(), objectMapper.writeValueAsString(responseEntity.getBody()));
                return;
            }

            response(resp, HttpServletResponse.SC_OK, objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 500, "Internal Server Error", requestURI);
            response(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, objectMapper.writeValueAsString(response));
            throw new RuntimeException(e);
        }
    }

    private void response(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(message);
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> queryParams = new ConcurrentHashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return queryParams;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0];
                String value = kv[1];
                queryParams.put(key, value);
            }
        }
        return queryParams;
    }
}
