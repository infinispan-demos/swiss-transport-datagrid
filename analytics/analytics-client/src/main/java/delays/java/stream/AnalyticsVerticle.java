package delays.java.stream;

import static delays.java.stream.AnalyticsUtil.timed;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import delays.java.stream.pojos.Stop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class AnalyticsVerticle extends AbstractVerticle {

   private RemoteCacheManager client;

   @Override
   public void start(Future<Void> future) throws Exception {
      System.out.println("Start analytics verticle");

      // Create a router object.
      Router router = Router.router(vertx);
      router.get("/delays-ratio").handler(this::getDelaysRatio);

      // Create the HTTP server and pass the "accept" method to the request handler.
      vertx
         .createHttpServer()
         .requestHandler(router::accept)
         .listen(
            // Retrieve the port from the configuration, default to 8080.
            config().getInteger("http.port", 8080), ar -> {
               if (ar.succeeded()) {
                  System.out.println("Server starter on port " + ar.result().actualPort());
               }
               future.handle(ar.mapEmpty());
            });

      deployInjectorVerticle();
   }

   private void deployInjectorVerticle() {
      DeploymentOptions options = new DeploymentOptions().setWorker(true);
      vertx.deployVerticle(InjectorVerticle.class.getName(), options);
   }

   @Override
   public void stop() throws Exception {
      if (client != null)
         client.stop();
   }

   private void getDelaysRatio(RoutingContext ctx) {
      System.out.println("Get delays ratio");
      Configuration cfg = RemoteDataGrid.config().get().build();
      client = new RemoteCacheManager(cfg);
      System.out.println("Create cache manager");

      RemoteCache<String, Stop> remote = client.getCache("analytics");
      List<Map<Integer, Long>> results = timed(() ->
            remote.execute("delay-ratio", Collections.emptyMap()), "calculate delayed ratio");

      JsonObject response = new JsonObject();
      response.put("delayed_per_hour", perHourJson(results.get(0)));
      response.put("tot_per_hour", perHourJson(results.get(1)));

      ctx.response()
            .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
            .end(response.encodePrettily());
   }

   private static JsonObject perHourJson(Map<Integer, Long> m) {
      Map<String, Object> copy = m.entrySet().stream()
            .collect(Collectors.toMap(
                  //e -> String.format("%02d", e.getKey()),
                  e -> e.getKey().toString(),
                  Map.Entry::getValue,
                  throwingMerger(),
                  TreeMap::new));
      return new JsonObject(copy);
   }

   private static <T> BinaryOperator<T> throwingMerger() {
      return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
   }

}
