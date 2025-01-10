package org.myspringframework.web.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myspringframework.annotations.Autowired;

import java.io.IOException;

public class DispatcherServlet extends HttpServlet {
    @Autowired
    private HandlerMapping handlerMapping;

    @Autowired
    private HandlerAdapter handlerAdapter;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());
        String httpMethod = req.getMethod();

        HandlerMethod handler = handlerMapping.getHandler(httpMethod, path);

        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
            return;
        }

        handlerAdapter.handle(req, resp, handler);
    }
}
