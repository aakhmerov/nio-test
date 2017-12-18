# NIO and WebFlux tests

The main goal of the project\repository is to figure out how async processing
is affecting REST services requests. Comparison of 2 approaches is taken as a
base:
  * Jersey based implementation with AsyncAnswer usage
  * Spring WebFlux based implementation

Theoretically both rely on Servlet 3.1 implementation and should perform similarly.


## How to use project 

you can run server from command line using maven

```
mvn clean package -DskipTests
mvn exec:java
```

Once server is started simply run _com.aakhmerov.test.ConnectionThreadsNumberTest_ 
in your favorite way, for example using maven 

```
mvn surefire:test -Dtest=ConnectionThreadsNumberTest
```

## Test results

results are printed into console and are free for interpretation. Sample results:

```
Running com.aakhmerov.test.ConnectionThreadsNumberTest
Endpoint: [http://localhost:8090/api/status/async/threaded-connection] 
 error count [0] 
 avg duration [4739] ms
Endpoint: [http://localhost:8090/api/status/async/unmanaged-threaded-connection] 
 error count [0] 
 avg duration [1958] ms
Endpoint: [http://localhost:8090/api/status/async/connection] 
 error count [0] 
 avg duration [1168] ms
Endpoint: [http://localhost:8090/flux/status/connection] 
 error count [0] 
 avg duration [5080] ms
Endpoint: [http://localhost:8090/api/status/connection] 
 error count [0] 
 avg duration [3821] ms
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 69.072 sec
```


## Known issues

### Linux: Limit of open files

if you see something like this in server logs

```
[qtp1142267663-13-acceptor-0@24be88c0-ServerConnector@2a796227{HTTP/1.1,[http/1.1]}{0.0.0.0:8090}] WARN  o.e.jetty.server.AbstractConnector - 
java.io.IOException: Too many open files
```

then you have to increase number of allowed cursors.

* https://stackoverflow.com/questions/2044672/ioexception-too-many-open-files 
