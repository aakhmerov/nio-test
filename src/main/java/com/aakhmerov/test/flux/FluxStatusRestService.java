package com.aakhmerov.test.flux;

import com.aakhmerov.test.dto.ConnectionStatusDto;
import com.aakhmerov.test.service.StatusCallable;
import com.aakhmerov.test.service.StatusCheckingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Askar Akhmerov
 */
@RestController
@RequestMapping(value = "/flux/status")
public class FluxStatusRestService {
  protected ExecutorService executor = Executors.newFixedThreadPool(10);

  @Autowired
  private StatusCheckingService statusCheckingService;

  @GetMapping("/connection")
  public Flux<ConnectionStatusDto> getConnectionStatus() {
    ConnectionStatusDto result = statusCheckingService.getConnectionStatus();
    return Flux.just(result);
  }

  @GetMapping("/threaded-connection")
  public Mono<ConnectionStatusDto> getThreadedConnectionStatus() throws Exception {

    StatusCallable result = new StatusCallable(statusCheckingService);
    executor.submit(result);

    return Mono.fromCallable(result);
  }
}
