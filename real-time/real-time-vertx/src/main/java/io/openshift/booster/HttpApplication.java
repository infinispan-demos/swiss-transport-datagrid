package io.openshift.booster;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

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

public class HttpApplication extends AbstractVerticle {

  private static final String template = "Hello, %s!";

  private volatile boolean injectorStarted = false;

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner r = new Runner("real-time-vertx/src/main/java/");
    r.runExample(HttpApplication.class);
  }

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
    PermittedOptions outPermit = new PermittedOptions().setAddress("delays");
    BridgeOptions options = new BridgeOptions().addOutboundPermitted(outPermit);
    sockJSHandler.bridge(options, be -> {
      if (be.type() == BridgeEventType.REGISTER) {
        System.out.println("SockJs: Connected, start data injector");
        if (!injectorStarted) {
          deployInjectorVerticle();
          injectorStarted = true;
        }
      }
      be.complete(true);
    });
    router.route("/eventbus/*").handler(sockJSHandler);

//    vertx.eventBus().consumer("delays", msg -> {
//      System.out.println("Received: " + msg.body());
//    });

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

  private void deployInjectorVerticle() {
    DeploymentOptions options = new DeploymentOptions().setWorker(true);
    vertx.deployVerticle("delays.query.continuous.InjectorVerticle", options);
  }

}
