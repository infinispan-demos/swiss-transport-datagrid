package delays.query.continuous.fx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

public class TestApp {

   public static void main(String[] args) {
      Vertx vertx = Vertx.vertx();
      HttpClient client = vertx.createHttpClient();
      client.websocket(80, "real-time-vertx-myproject.172.20.10.4.xip.io", "/eventbus/websocket", ws -> {
         System.out.println("Connected");

         // Register
         JsonObject msg = new JsonObject().put("type", "register").put("address", "some-address");
         ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

         ws.handler(buff -> {
            System.out.println("Buff...");
            String str = buff.toString();
            System.out.println(str);
         });
      });
   }

}
