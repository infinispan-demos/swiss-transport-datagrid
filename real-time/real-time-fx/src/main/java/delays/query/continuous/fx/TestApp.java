package delays.query.continuous.fx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;

public class TestApp {

   static final String HTTP_HOST = System.getProperty("http.host", "real-time-vertx-myproject.127.0.0.1.xip.io");
   static final int HTTP_PORT = Integer.getInteger("http.port", 80);

   public static void main(String[] args) {
      Vertx vertx = Vertx.vertx();
      HttpClient client = vertx.createHttpClient();
      client.websocket(HTTP_PORT, HTTP_HOST, "/eventbus/websocket", ws -> {
         System.out.println("Connected");
         sendPing(ws);

         vertx.setPeriodic(5000, id -> {
            sendPing(ws);
         });

         // Register
         JsonObject msg = new JsonObject().put("type", "register").put("address", "delays");
         ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

         ws.handler(buff -> {
            System.out.println(buff);
         });
      }, fail -> {
         System.out.println("Failed: " + fail);
      });
   }

   static void sendPing(WebSocket ws) {
      JsonObject msg = new JsonObject().put("type", "ping");
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));
   }

}
