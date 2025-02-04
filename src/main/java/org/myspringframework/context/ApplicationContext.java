package org.myspringframework.context;


import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.myspringframework.annotations.Autowired;
import org.myspringframework.annotations.Component;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ApplicationContext {
    private Map<Class<?>, Object> beanRegistry;

    public ApplicationContext() {
        this.beanRegistry = new ConcurrentHashMap<>();
    }

    public void refresh(Class<?> primarySource) {
        scanAndRegisterBeans(primarySource.getPackageName());
        autowire();
    }

    public void scanAndRegisterBeans(String basePackage) {
        List<Class<?>> classes = scanClasses(basePackage);
        for (Class<?> clazz : classes) {
            if (isComponent(clazz)) {
                registerBean(clazz);
            }
        }
    }

    private List<Class<?>> scanClasses(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource == null) {
            throw new RuntimeException("Resource not found for base package: " + basePackage);
        }

        try {
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                classes.addAll(scanClassesFromFileSystem(new File(resource.toURI()), basePackage));
            } else if ("jar".equals(protocol)) {
                classes.addAll(scanClassesFromJar(resource, path));
            } else {
                throw new RuntimeException("Unsupported protocol: " + protocol);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan classes in package: " + basePackage, e);
        }
        return classes;
    }

    private List<Class<?>> scanClassesFromFileSystem(File directory, String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        // 디렉토리가 존재하지 않으면 빈 리스트 반환
        if (!directory.exists()) {
            return classes;
        }

        // 디렉토리 내 모든 파일 및 디렉토리에 대해 클래스 파일 스캔
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            // 디렉토리인 경우 재귀적으로 클래스 파일 스캔
            if (file.isDirectory()) {
                classes.addAll(scanClassesFromFileSystem(file, packageName + "." + file.getName()));
            }
            // 클래스 파일인 경우 클래스로 등록
            else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    private List<Class<?>> scanClassesFromJar(URL resource, String path) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        // 실제 JAR 파일 경로 추출 (예: "file:/path/to.jar" 부분 추출)
        String resourcePath = resource.getPath();
        int separatorIndex = resourcePath.indexOf("!/");
        String jarPath = resourcePath.substring(0, separatorIndex);

        // JAR 파일 경로에서 "file:" 제거
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5); // "file:" 제거
        }

        // JAR 파일 내 클래스 파일 스캔
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            // JAR 파일 내 모든 엔트리에 대해 클래스 파일 스캔
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 지정된 패키지 경로에 속하고 클래스 파일인 경우 클래스로 등록
                if (entryName.startsWith(path) && entryName.endsWith(".class") && !entry.isDirectory()) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
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

