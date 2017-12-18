package com.aakhmerov.test.jetty;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.JettyHttpHandlerAdapter;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import javax.servlet.DispatcherType;
import java.net.URL;
import java.util.EnumSet;

/**
 * @author Askar Akhmerov
 */
public class SpringAwareServletConfiguration implements ApplicationContextAware {

  private static final String COMPRESSED_MIME_TYPES = "application/json," +
      "text/html," +
      "application/x-font-ttf," +
      "image/svg+xml";

  private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";
  private String springContextLocation = "classpath:applicationContext.xml";
  private String restPackage = "com.aakhmerov.test.jersey";
  private ContextLoaderListener contextLoaderListener = new ContextLoaderListener();
  private ApplicationContext applicationContext;
  private ServletContextHandler servletContextHandler;

  public SpringAwareServletConfiguration() {
  }

  public SpringAwareServletConfiguration(String contextLocation) {
    this.springContextLocation = contextLocation;
  }

  private ServletHolder initJerseyServlet() {
    // Create JAX-RS application.
    final ResourceConfig application = new ResourceConfig()
        .packages(restPackage)
        .register(JacksonFeature.class);

    ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(application));
    jerseyServlet.setInitOrder(1);
    return jerseyServlet;
  }

  private ServletContextHandler setupServletContextHandler() {
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

    //add spring
    context.addEventListener(contextLoaderListener);
    context.setInitParameter(CONTEXT_CONFIG_LOCATION, springContextLocation);
    context.addLifeCycleListener(new SpringContextInterceptingListener(this));

    addStaticResources(context);
    return context;
  }

  private void addStaticResources(ServletContextHandler context) {
    //add static resources
    URL webappURL = this.getClass().getClassLoader().getResource("webapp");
    if (webappURL != null) {
      context.setResourceBase(webappURL.toExternalForm());
    }
    ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
    holderPwd.setInitParameter("dirAllowed", "true");
    context.addServlet(holderPwd, "/");

    NotFoundErrorHandler errorMapper = new NotFoundErrorHandler();
    context.setErrorHandler(errorMapper);

    initGzipFilterHolder(context);
  }

  private void initGzipFilterHolder(ServletContextHandler context) {
    FilterHolder gzipFilterHolder = new FilterHolder(GzipFilter.class);
    gzipFilterHolder.setInitParameter("deflateCompressionLevel", "9");
    gzipFilterHolder.setInitParameter("minGzipSize", "0");
    gzipFilterHolder.setInitParameter("mimeTypes", COMPRESSED_MIME_TYPES);

    context.addFilter(gzipFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  public ServletContextHandler getServletContextHandler() {
    if (servletContextHandler == null) {
      servletContextHandler = setupServletContextHandler();
    }
    return servletContextHandler;
  }

  private ServletHolder initFluxServlet() {
    HttpHandler handler = WebHttpHandlerBuilder.applicationContext(applicationContext).build();
    ServletHolder fluxServlet = new ServletHolder(new JettyHttpHandlerAdapter(handler));
    fluxServlet.setInitOrder(0);
    return fluxServlet;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;

    ServletHolder fluxServlet = initFluxServlet();
    ServletHolder jerseyServlet = initJerseyServlet();

    servletContextHandler.setContextPath("/");
    servletContextHandler.addServlet(jerseyServlet, "/api/*");

    servletContextHandler.addServlet(fluxServlet, "/flux/*");

    try {
      servletContextHandler.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
