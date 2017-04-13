package io.openshift.booster;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

public class HttpApplication extends AbstractVerticle {

  private static final String template = "Hello, %s!";

  @Override
  public void start(Future<Void> future) {
    // Create a router object.
    Router router = Router.router(vertx);

    router.get("/api/greeting").handler(this::greeting);
    router.get("/").handler(StaticHandler.create());

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

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    PermittedOptions outboundPermitted = new PermittedOptions().setAddress("some-address");
    BridgeOptions options = new BridgeOptions().addOutboundPermitted(outboundPermitted);
    sockJSHandler.bridge(options, be -> {
      if (be.type() == BridgeEventType.REGISTER) {
        System.out.println("sockJs: connected");
        vertx.eventBus().publish("some-address", "hey all, we have a new subscriber ");
      }
      be.complete(true);
    });
    router.route("/eventbus/*").handler(sockJSHandler);

    deployRealTimeVerticle();
  }

  private void deployRealTimeVerticle() {
    DeploymentOptions options = new DeploymentOptions().setWorker(true);
    vertx.deployVerticle("delays.query.continuous.RealTimeVerticle", options);
  }

  private void greeting(RoutingContext rc) {
    String name = rc.request().getParam("name");
    if (name == null) {
      name = "World";
    }

    JsonObject response = new JsonObject()
        .put("content", String.format(template, name));

    rc.response()
        .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
        .end(response.encodePrettily());
  }
}
