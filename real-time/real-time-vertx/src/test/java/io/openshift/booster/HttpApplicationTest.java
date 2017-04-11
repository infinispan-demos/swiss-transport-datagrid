package io.openshift.booster;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class HttpApplicationTest {

    private Vertx vertx;
    private WebClient client;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        vertx.deployVerticle(HttpApplication.class.getName(), context.asyncAssertSuccess());
        client = WebClient.create(vertx);
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void callGreetingTest(TestContext context) {
        // Send a request and get a response
        Async async = context.async();
        client.get(8080, "localhost", "/api/greeting")
            .send(resp -> {
                assertThat(resp.succeeded()).isTrue();
                assertThat(resp.result().statusCode()).isEqualTo(200);
                String content = resp.result().bodyAsJsonObject().getString("content");
                assertThat(content).isEqualTo("Hello, World!");
                async.complete();
            });
    }

    @Test
    public void callGreetingWithParamTest(TestContext context) {
        // Send a request and get a response
        Async async = context.async();
        client.get(8080, "localhost", "/api/greeting?name=Charles")
            .send(resp -> {
                    assertThat(resp.succeeded()).isTrue();
                    assertThat(resp.result().statusCode()).isEqualTo(200);
                    String content = resp.result().bodyAsJsonObject().getString("content");
                    assertThat(content).isEqualTo("Hello, Charles!");
                    async.complete();
            });
    }

}
