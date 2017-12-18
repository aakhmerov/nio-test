package com.aakhmerov.test.service;

import com.aakhmerov.test.dto.ConnectionStatusDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StatusCheckingService {

  public ConnectionStatusDto getConnectionStatus() {
    //just simulate doing something for some time
    try {
      Thread.sleep((long) (Math.random() * 1000));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    ConnectionStatusDto status = new ConnectionStatusDto();
    status.setConnectedToElasticsearch(isConnectedToElasticSearch());
    Map<String, Boolean> engineConnections = new HashMap<>();
    engineConnections.put("test", true);
    status.setEngineConnections(engineConnections);
    return status;
  }


  private boolean isConnectedToElasticSearch() {
    return false;
  }


}
