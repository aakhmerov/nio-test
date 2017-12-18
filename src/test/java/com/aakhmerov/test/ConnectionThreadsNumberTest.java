package com.aakhmerov.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple test to verify performance of REST endpoints in arbitrary setup.
 *
 * MAX_THREADS threads are started at the same point of time all sending
 * request to the same endpoint. Let's see who will survive :)
 *
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class ConnectionThreadsNumberTest {


  private static final int MAX_THREADS = 3500;
  public static final String SYNC_ENDPOINT = "http://localhost:8090/api/status/connection";
  public static final String ASYNC_ENDPOINT = "http://localhost:8090/api/status/async/connection";
  public static final String THREADED_ASYNC_ENDPOINT = "http://localhost:8090/api/status/async/threaded-connection";
  public static final String UNMANAGED_THREADED_ASYNC_ENDPOINT = "http://localhost:8090/api/status/async/unmanaged-threaded-connection";
  public static final String FLUX_ENDPOINT = "http://localhost:8090/flux/status/connection";
  private AtomicInteger errorCount = new AtomicInteger(0);

  private String endpoint;
  private AtomicLong duration = new AtomicLong(0L);

  public ConnectionThreadsNumberTest(String endpoint) {
    this.endpoint = endpoint;
  }

  @Parameterized.Parameters
  public static Collection<String> data() {
    List<String> list = Arrays.asList(new String[]{
        ASYNC_ENDPOINT,
        SYNC_ENDPOINT,
        THREADED_ASYNC_ENDPOINT,
        UNMANAGED_THREADED_ASYNC_ENDPOINT,
        FLUX_ENDPOINT
    });
    Collections.shuffle(list);
    return list;
  }

  @Test
  public void testEndpointPerformance() throws Exception {
    CountDownLatch count = new CountDownLatch(MAX_THREADS - 1);
    CountDownLatch endCount = new CountDownLatch(MAX_THREADS - 1);
    for (int i = 0; i < MAX_THREADS; i++) {
      ConnectionThread worker = new ConnectionThread(i, count, endCount);
      worker.start();
      count.countDown();
    }
    endCount.await();
    long avgDuration = duration.get() / MAX_THREADS;

    System.out.println("Endpoint: [" + endpoint + "] \n" +
        " error count [" + errorCount.get() + "] \n" +
        " avg duration [" + avgDuration + "] ms");
  }

  public class ConnectionThread extends Thread {
    private CloseableHttpClient httpclient;
    private CountDownLatch countDownLatch;
    private CountDownLatch endLatch;
    private int id;

    public ConnectionThread(int i, CountDownLatch count, CountDownLatch countDownLatch) {
      id = i;
      httpclient = HttpClients.custom()
          .disableAutomaticRetries()
          .build();
      this.countDownLatch = count;
      endLatch = countDownLatch;
    }

    public void run() {
      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      long start = System.currentTimeMillis();

      HttpGet httpGet = new HttpGet(endpoint);
      CloseableHttpResponse response = null;
      try {
        response = httpclient.execute(httpGet);
      } catch (IOException e) {
        //just increment counter
        errorCount.incrementAndGet();
      }

      long end = System.currentTimeMillis();
      long localDuration = end - start;
      duration.addAndGet(localDuration);

      if (response != null) {
        consumeAndClose(response, localDuration);
      }
      endLatch.countDown();
    }

    private void consumeAndClose(CloseableHttpResponse response, long localDuration) {
      try {


        if (response.getStatusLine().getStatusCode() != 200) {
          errorCount.incrementAndGet();
        }
        //System.out.println("[" + id + "] " + response.getStatusLine() + " took [" + localDuration + "] ms");
        HttpEntity entity1 = response.getEntity();
        // do something useful with the response body
        // and ensure it is fully consumed
        EntityUtils.consume(entity1);


      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          response.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}