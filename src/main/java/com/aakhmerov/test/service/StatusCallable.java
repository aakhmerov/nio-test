package com.aakhmerov.test.service;

import com.aakhmerov.test.dto.ConnectionStatusDto;

import java.util.concurrent.Callable;

/**
 * @author Askar Akhmerov
 */
public class StatusCallable implements Callable <ConnectionStatusDto> {
  private StatusCheckingService service;

  public StatusCallable(StatusCheckingService service) {
    this.service = service;
  }

  @Override
  public ConnectionStatusDto call() throws Exception {
    return service.getConnectionStatus();
  }
}
