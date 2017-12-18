package com.aakhmerov.test.jersey;

import com.aakhmerov.test.dto.ConnectionStatusDto;
import com.aakhmerov.test.service.StatusCheckingService;
import org.glassfish.jersey.server.ManagedAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

@Path("/status/async")
@Component
public class AsyncStatusRestService {

  @Autowired
  private StatusCheckingService statusCheckingService;

  @GET
  @Path("/connection")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.WILDCARD)
  @ManagedAsync
  public void getConnectionStatus(@Suspended final AsyncResponse asyncResponse) {
    ConnectionStatusDto result = statusCheckingService.getConnectionStatus();
    asyncResponse.resume(result);
  }

  @GET
  @Path("/threaded-connection")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.WILDCARD)
  @ManagedAsync
  public void getThreadedConnectionStatus(@Suspended final AsyncResponse asyncResponse) {
    new Thread(() -> {
      ConnectionStatusDto result = statusCheckingService.getConnectionStatus();
      asyncResponse.resume(result);
    }).start();
  }

  @GET
  @Path("/unmanaged-threaded-connection")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.WILDCARD)
  public void getUnmanagedThreadedConnectionStatus(@Suspended final AsyncResponse asyncResponse) {
    new Thread(() -> {
      ConnectionStatusDto result = statusCheckingService.getConnectionStatus();
      asyncResponse.resume(result);
    }).start();
  }


}
