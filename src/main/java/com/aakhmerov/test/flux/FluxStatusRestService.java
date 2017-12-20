package com.aakhmerov.test.flux;

import com.aakhmerov.test.dto.ConnectionStatusDto;
import com.aakhmerov.test.service.StatusCheckingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Askar Akhmerov
 */
@RestController
@RequestMapping(value = "/flux/status")
public class FluxStatusRestService {

  @Autowired
  private StatusCheckingService statusCheckingService;

  @GetMapping("/connection")
  public Mono<ConnectionStatusDto> getConnectionStatus() {
    ConnectionStatusDto result = statusCheckingService.getConnectionStatus();
    return Mono.just(result);
  }

  @GetMapping("/threaded-connection")
  public Mono<ConnectionStatusDto> getThreadedConnectionStatus() throws Exception {
    final ConnectionStatusDto[] result = new ConnectionStatusDto[1];

    Thread thread = new Thread(() -> {
      result[0] = statusCheckingService.getConnectionStatus();
    });
    thread.start();
    thread.join();

    return Mono.just(result[0]);
  }
}
