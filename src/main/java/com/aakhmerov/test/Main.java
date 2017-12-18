package com.aakhmerov.test;


import com.aakhmerov.test.jetty.EmbeddedJettyServer;

/**
 * @author Askar Akhmerov
 */
public class Main {

  private static EmbeddedJettyServer jettyServer;

  public static void main(String[] args) throws Exception {
    jettyServer = new EmbeddedJettyServer();
    try {
      jettyServer.startServer();
      jettyServer.join();
    } finally {
      jettyServer.destroyServer();
    }
  }


}
