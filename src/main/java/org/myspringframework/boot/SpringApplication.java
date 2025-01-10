package org.myspringframework.boot;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.myspringframework.annotations.SpringBootApplication;
import org.myspringframework.context.ApplicationContext;
import org.myspringframework.web.servlet.DispatcherServlet;

public class SpringApplication {
    private Class<?> primarySource;

    public SpringApplication(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    public ApplicationContext run(String... args) {
        printBanner();

        ApplicationContext context = new ApplicationContext();
        context.refresh(primarySource);

        startServer(context);

        return context;
    }

    private void startServer(ApplicationContext context) {
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");

        DispatcherServlet dispatcherServlet = context.getBean(DispatcherServlet.class);
        ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
        handler.addServlet(servletHolder, "/*");

        server.setHandler(handler);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start the server", e);
        }
    }

    public static ApplicationContext run(Class<?> primarySource, String... args) {
        if (!primarySource.isAnnotationPresent(SpringBootApplication.class)) {
            throw new RuntimeException("The Application class should be annotated with @SpringBootApplication");
        }

        return (new SpringApplication(primarySource)).run(args);
    }

    private void printBanner() {
        String banner =
                """
                         __  __       ____             _             ____              _  \s
                        |  \\/  |_   _/ ___| _ __  _ __(_)_ __   __ _| __ )  ___   ___ | |_\s
                        | |\\/| | | | \\___ \\| '_ \\| '__| | '_ \\ / _` |  _ \\ / _ \\ / _ \\| __|
                        | |  | | |_| |___) | |_) | |  | | | | | (_| | |_) | (_) | (_) | |_\s
                        |_|  |_|\\__, |____/| .__/|_|  |_|_| |_|\\__, |____/ \\___/ \\___/ \\__|
                                |___/      |_|                 |___/                      \s
                                        
                """;

        System.out.println(banner);
    }
}
