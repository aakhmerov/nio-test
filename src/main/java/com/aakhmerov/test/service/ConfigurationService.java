package com.aakhmerov.test.service;

/**
 * @author Askar Akhmerov
 */
public class ConfigurationService {
  public String getContainerHost() {
    return "0.0.0.0";
  }

  public String getContainerKeystorePassword() {
    return "test123";
  }

  public String getContainerKeystoreLocation() {
    return "keystore.jks";
  }

  public int getContainerHttpsPort() {
    return 8091;
  }

  public int getContainerHttpPort() {
    return 8090;
  }
}
