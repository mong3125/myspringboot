package org.myspringframework.context;


import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.myspringframework.annotations.Autowired;
import org.myspringframework.annotations.Component;
import org.myspringframework.mapper.JsonParser;
import org.myspringframework.mapper.ObjectMapper;
import org.myspringframework.web.servlet.DispatcherServlet;
import org.myspringframework.web.servlet.HandlerAdapter;
import org.myspringframework.web.servlet.HandlerMapping;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {
    private Map<Class<?>, Object> beanRegistry;

    public ApplicationContext() {
        this.beanRegistry = new ConcurrentHashMap<>();
    }

    public void refresh(Class<?> primarySource) {
        scanAndRegisterBeans(primarySource.getPackageName());
        registerSpringBean();
        autowire();
    }

    private void registerSpringBean() {
        registerBean(HandlerMapping.class, new HandlerMapping(this));
        registerBean(HandlerAdapter.class, new HandlerAdapter());
        registerBean(DispatcherServlet.class, new DispatcherServlet());
        registerBean(ObjectMapper.class, new ObjectMapper());
        registerBean(JsonParser.class, new JsonParser());
    }

    public void scanAndRegisterBeans(String basePackage) {
        try {
            String path = basePackage.replace('.', '/');
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource == null) {
                return;
            }
            File directory = new File(resource.toURI());
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) {
                    scanAndRegisterBeans(basePackage + '.' + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = basePackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);

                    if (isComponent(clazz)) {
                        registerBean(clazz);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan and register beans", e);
        }
    }

    private boolean isComponent(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }

        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Component.class)) {
                return true;
            }
        }

        return false;
    }

    public <T> void registerBean(Class<T> clazz) {
        try {
            Class<? extends T> proxyClass = new ByteBuddy()
                    .subclass(clazz)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new ByteBuddyInterceptor()))
                    .make()
                    .load(clazz.getClassLoader())
                    .getLoaded();

            T proxy = proxyClass.getDeclaredConstructor().newInstance();

            registerBean(clazz, proxy);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register bean", e);
        }
    }

    public void registerBean(Class<?> clazz, Object bean) {
        this.beanRegistry.put(clazz, bean);
    }

    private void autowire() {
        try {
            for (Object bean : beanRegistry.values()) {
                Object target = bean;
                Class<?> targetClass = bean.getClass();
                if (bean.getClass().getName().contains("ByteBuddy")) {
                    targetClass = bean.getClass().getSuperclass();
                }

                for (Field field : targetClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        field.setAccessible(true);
                        Object dependency = beanRegistry.get(field.getType());
                        if (dependency == null) {
                            throw new RuntimeException("Failed to autowire bean of type: " + field.getType().getName());
                        }
                        field.set(target, dependency);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fail to process autowiring");
        }
    }

    public <T> T getBean(Class<T> clazz) {
        Object bean = this.beanRegistry.get(clazz);
        if (bean == null) {
            throw new RuntimeException("Bean not found: " + clazz.getName());
        }
        return clazz.cast(this.beanRegistry.get(clazz));
    }

    public Map<Class<?>, Object> getBeanRegistry() {
        return beanRegistry;
    }
}

