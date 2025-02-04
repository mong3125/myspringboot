package org.myspringframework.context;

import org.myspringframework.mapper.JsonParser;
import org.myspringframework.mapper.ObjectMapper;
import org.myspringframework.web.servlet.DispatcherServlet;
import org.myspringframework.web.servlet.HandlerAdapter;
import org.myspringframework.web.servlet.HandlerMapping;

public class InternalBeanRegistrar {
    public void registerBeans(ApplicationContext context) {
        context.registerBean(HandlerMapping.class, new HandlerMapping(context));
        context.registerBean(HandlerAdapter.class, new HandlerAdapter());
        context.registerBean(DispatcherServlet.class, new DispatcherServlet());
        context.registerBean(ObjectMapper.class, new ObjectMapper());
        context.registerBean(JsonParser.class, new JsonParser());
    }
}
