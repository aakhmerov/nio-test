package com.aakhmerov.test.jetty;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.aakhmerov.test.service.ConfigurationService;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Jetty embedded server wrapping jersey servlet handler and loading properties from
 * service and environment property files.
 *
 * @author Askar Akhmerov
 */
public class EmbeddedJettyServer {

  private static final String PROTOCOL = "http/1.1";

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedJettyServer.class);

  private SpringAwareServletConfiguration jerseyServlet;
  private Server jettyServer;

  public EmbeddedJettyServer() {
    jerseyServlet = new SpringAwareServletConfiguration();
    jettyServer = setUpEmbeddedJetty(jerseyServlet);
    disableServerSignature(jettyServer);
  }

  private void disableServerSignature(Server jettyServer) {
    for (Connector y : jettyServer.getConnectors()) {
      for (ConnectionFactory x : y.getConnectionFactories()) {
        if (x instanceof HttpConnectionFactory) {
          ((HttpConnectionFactory) x).getHttpConfiguration().setSendServerVersion(false);
        }
      }
    }
  }


  private Server setUpEmbeddedJetty(SpringAwareServletConfiguration jerseyServlet) {
    defineLogbackLoggingConfiguration();
    ConfigurationService configurationService = constructConfigurationService();

    Server jettyServer = initServer(configurationService);

    ServletContextHandler context = jerseyServlet.getServletContextHandler();

    jettyServer.setHandler(context);
    return jettyServer;
  }

  private ConfigurationService constructConfigurationService() {
    return new ConfigurationService();
  }

  private void defineLogbackLoggingConfiguration() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    loggerContext.reset();
    JoranConfigurator configurator = new JoranConfigurator();
    InputStream configStream = null;
    try {
      configStream = getLogbackConfigurationFileStream();
      configurator.setContext(loggerContext);
      configurator.doConfigure(configStream); // loads logback file
      configStream.close();
    } catch (JoranException | IOException e) {
      //since logging setup broke, print it in standard error stream
      e.printStackTrace();
    } finally {
      if (configStream != null) {
        try {
          configStream.close();
        } catch (IOException e) {
          logger.error("error closing stream", e);
        }
      }
    }
  }

  private InputStream getLogbackConfigurationFileStream() {
    InputStream stream = this.getClass().getClassLoader().getResourceAsStream("environment-logback.xml");
    if (stream != null) {
      return stream;
    }
    stream = this.getClass().getClassLoader().getResourceAsStream("logback-test.xml");
    if (stream != null) {
      return stream;
    }
    stream = this.getClass().getClassLoader().getResourceAsStream("logback.xml");
    if (stream != null) {
      return stream;
    }
    return null;
  }


  private Server initServer(ConfigurationService configurationService) {
    String host = configurationService.getContainerHost();
    String keystorePass = configurationService.getContainerKeystorePassword();
    String keystoreLocation = configurationService.getContainerKeystoreLocation();
    Server server = new Server();

    ServerConnector connector = initHttpConnector(configurationService, host, server);

    ServerConnector sslConnector = initHttpsConnector(configurationService, host, keystorePass, keystoreLocation, server);

    server.setConnectors(new Connector[]{connector, sslConnector});

    return server;
  }

  private ServerConnector initHttpsConnector(ConfigurationService configurationService, String host, String keystorePass, String keystoreLocation, Server server) {
    HttpConfiguration https = new HttpConfiguration();
    https.setSendServerVersion(false);
    https.addCustomizer(new SecureRequestCustomizer());
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath(this.getClass().getClassLoader().getResource(keystoreLocation).toExternalForm());
    sslContextFactory.setKeyStorePassword(keystorePass);
    sslContextFactory.setKeyManagerPassword(keystorePass);

    ServerConnector sslConnector = new ServerConnector(server,
        new SslConnectionFactory(sslContextFactory, PROTOCOL),
        new HttpConnectionFactory(https));
    sslConnector.setPort(configurationService.getContainerHttpsPort());
    sslConnector.setHost(host);
    return sslConnector;
  }

  private ServerConnector initHttpConnector(ConfigurationService configurationService, String host, Server server) {
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(configurationService.getContainerHttpPort());
    connector.setHost(host);
    return connector;
  }

  /**
   * Since spring context is not loaded yet, just hook up properties manually.
   */
  private Properties getProperties() {
    Properties result = new Properties();
    try {
      result.load(
          this.getClass().getClassLoader()
              .getResourceAsStream("service.properties")
      );
      InputStream environmentProperties = this.getClass().getClassLoader()
          .getResourceAsStream("environment.properties");
      if (environmentProperties != null) {
        //overwrites previously loaded properties
        result.load(environmentProperties);
      }
    } catch (IOException e) {
      logger.error("cant read properties", e);
    }
    return result;
  }

  public void startServer() throws Exception {
    this.jettyServer.start();
  }

  public void join() throws InterruptedException {
    jettyServer.join();
  }

  public void destroyServer() throws Exception {
    jettyServer.stop();
    jettyServer.destroy();
  }
}
