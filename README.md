# NIO and WebFlux tests

The main goal of the project\repository is to figure out how async processing
is affecting REST services requests. Comparison of 2 approaches is taken as a
base:
  * Jersey based implmentation with AsyncAnswer usage
  * Spring WebFlux based implementation

Theoretically both rely on Servlet 3.1 implementation and should perform similarly.
