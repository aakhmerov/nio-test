package com.aakhmerov.test.dto;

import java.util.Map;

public class ConnectionStatusDto {

  protected Map<String, Boolean> engineConnections;
  protected boolean isConnectedToElasticsearch;

  public Map<String, Boolean> getEngineConnections() {
    return engineConnections;
  }

  public void setEngineConnections(Map<String, Boolean> engineConnections) {
    this.engineConnections = engineConnections;
  }

  /**
   * True if connected to the Elasticsearch, false otherwise.
   */
  public boolean isConnectedToElasticsearch() {
    return isConnectedToElasticsearch;
  }

  public void setConnectedToElasticsearch(boolean connectedToElasticsearch) {
    isConnectedToElasticsearch = connectedToElasticsearch;
  }
}
