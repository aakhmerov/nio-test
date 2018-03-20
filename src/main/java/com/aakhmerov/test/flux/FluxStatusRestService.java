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
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Askar Akhmerov
 */
@RestController
@RequestMapping(value = "/flux/status")
public class FluxStatusRestService {

  private StatusCheckingService statusCheckingService;
  private Scheduler scheduler;

  @Autowired
  public FluxStatusRestService(StatusCheckingService statusCheckingService) {
    this.statusCheckingService = statusCheckingService;
    this.scheduler = Schedulers.newParallel("sub");
    this.scheduler.start();
  }

  /**
   * This implementation is blocking since status service will cause thread to sleep
   * explicitly.
   *
   * @return
   */
  @GetMapping("/connection")
  public Flux<ConnectionStatusDto> getConnectionStatus() {
    ConnectionStatusDto result = statusCheckingService.getConnectionStatus();
    return Flux.just(result);
  }

  /**
   * Non-blocking implementation, since executor thread will be still sleeping,
   * buth up to 400 simultaneous connections will be allowed into request queue.
   *
   * @return
   * @throws Exception
   */
  @GetMapping("/threaded-connection")
  public Mono<ConnectionStatusDto> getThreadedConnectionStatus() throws Exception {
    StatusCallable result = new StatusCallable(statusCheckingService);
    return Mono.fromCallable(result).publishOn(this.scheduler);
  }
}
