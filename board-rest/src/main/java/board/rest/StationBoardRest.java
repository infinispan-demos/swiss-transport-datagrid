package board.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import board.rest.vertx.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Verify by visiting these URLs:
 *    http://localhost:8080/stationboard
 *    http://localhost:8080/stationboard/8500081
 *    http://localhost:8080/stationboard/8500090
 */
public class StationBoardRest extends AbstractVerticle {

   private static final boolean PRETTY_JSON = true;

   private Map<Long, JsonObject> boards = new HashMap<>();

   // Convenience method so you can run it in your IDE
   public static void main(String[] args) {
      Runner r = new Runner("board-rest/src/main/java/");
      r.runExample(StationBoardRest.class);
   }

   @Override
   public void start() {
      setUpInitialData();

      Router router = Router.router(vertx);

      router.route().handler(BodyHandler.create());
      router.get("/stationboard/:boardId").handler(this::handleGetBoard);
      router.get("/stationboard").handler(this::handleListBoards);

      vertx.createHttpServer().requestHandler(router::accept).listen(8080);
   }

   private void handleGetBoard(RoutingContext ctx) {
      Long boardId = Long.valueOf(ctx.request().getParam("boardId"));
      HttpServerResponse response = ctx.response();
      if (boardId == null) {
         sendError(400, response);
      } else {
         JsonObject board = boards.get(boardId);
         if (board == null) {
            sendError(404, response);
         } else {
            HttpServerResponse rsp = response.putHeader("content-type", "application/json");
            if (PRETTY_JSON)
               rsp.end(board.encodePrettily());
            else
               rsp.end();
         }
      }
   }

   //   {"timestamp":1489745723891,
   //       "stats":
   //        [
   //          {"timestamp":1489745720247,"stop":{"id":8500081,"name":"Altmarkt","location":{"lat":47.47642,"lng":7.74471}},"total":2,"delayed":0},
   //          {"timestamp":1489745722329,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},"total":5,"delayed":1}
   //        ]
   //   }
   private void handleListBoards(RoutingContext ctx) {
      JsonObject json = new JsonObject()
         .put("timestamp", 1489745723891L)
         .put("stats", new JsonArray(Arrays.asList(
            new JsonObject()
               .put("timestamp", 1489745720247L)
               .put("stop", getAltmarktStop())
               .put("total", 2)        // TODO: calculate from boards
               .put("delayed", 0),     // TODO: calculate from boards
            new JsonObject()
               .put("timestamp", 1489745722329L)
               .put("stop", getBaselBadStop())
               .put("total", 5)     // TODO: calculate from boards
               .put("delayed", 1)   // TODO: calculate from boards
         )));
      HttpServerResponse rsp = ctx.response().putHeader("content-type", "application/json");
      if (PRETTY_JSON)
         rsp.end(json.encodePrettily());
      else
         rsp.end();
   }

   private void sendError(int statusCode, HttpServerResponse response) {
      response.setStatusCode(statusCode).end();
   }

   private void setUpInitialData() {
      addBoard(getAltmarktBoard());
      addBoard(getBaselBadBoard());
   }

   private void addBoard(JsonObject board) {
      boards.put(board.getJsonObject("stop").getLong("id"), board);
   }

   //   {"timestamp":1489745720247,"stop":{"id":8500081,"name":"Altmarkt","location":{"lat":47.47642,"lng":7.74471}},
   //   "events":
   //      {
   //         "R 3168/Waldenburg/2016-02-29T17:07:00+0100":{"timestamp":1489745720246,"stop":{"id":8500081,"name":"Altmarkt","location":{"lat":47.47642,"lng":7.74471}},"train":{"id":"R 3168/Waldenburg/2016-02-29T17:07:00+0100","category":"R","name":"R 3168","lastStopName":"Waldenburg"},"arrivalTimestamp":32984109000,"departureTimestamp":1489746129000,"platform":""},
   //         "R 3169/Liestal/2016-02-29T17:12:00+0100":{"timestamp":1489745720247,"stop":{"id":8500081,"name":"Altmarkt","location":{"lat":47.47642,"lng":7.74471}},"train":{"id":"R 3169/Liestal/2016-02-29T17:12:00+0100","category":"R","name":"R 3169","lastStopName":"Liestal"},"arrivalTimestamp":32984109000,"departureTimestamp":1489746429000,"platform":""}
   //      }
   //   }
   private JsonObject getAltmarktBoard() {
      JsonObject stop = getAltmarktStop();
      return new JsonObject()
         .put("timestamp", 1489745720247L)
         .put("stop", stop)
         .put("events", new JsonArray(Arrays.asList(
               new JsonObject().put("R 3168/Waldenburg/2016-02-29T17:07:00+0100", new JsonObject()
                  .put("timestamp", 1489745720246L)
                  .put("stop", stop)
                  .put("train", new JsonObject()
                     .put("id", "R 3168/Waldenburg/2016-02-29T17:07:00+0100")
                     .put("category", "R")
                     .put("name", "R 3168")
                     .put("lastStopName", "Waldenburg"))
                  .put("arrivalTimestamp", 32984109000L)
                  .put("departureTimestamp", 1489746129000L)
                  .put("platform", "")
               ),
               new JsonObject().put("R 3169/Liestal/2016-02-29T17:12:00+0100", new JsonObject()
                     .put("timestamp", 1489745720247L)
                     .put("stop", stop)
                     .put("train", new JsonObject()
                           .put("id", "R 3169/Liestal/2016-02-29T17:12:00+0100")
                           .put("category", "R")
                           .put("name", "R 3169")
                           .put("lastStopName", "Liestal"))
                     .put("arrivalTimestamp", 32984109000L)
                     .put("departureTimestamp", 1489746429000L)
                     .put("platform", "")
               )
         )));
   }

   private JsonObject getAltmarktStop() {
      return new JsonObject()
               .put("id", 8500081)
               .put("name", "Altmarkt")
               .put("location",
                     new JsonObject()
                           .put("lat", 47.47642)
                           .put("lng", 7.74471));
   }

   // Basel Bad board includes a delayed train

   //   {"timestamp":1489745722329,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},
   //      "events":
   //      {
   //         "ICE 376/Frankfurt (Main) Hbf/2016-02-29T17:15:00+0100":{"timestamp":1489745722321,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},"train":{"id":"ICE 376/Frankfurt (Main) Hbf/2016-02-29T17:15:00+0100","category":"ICE","name":"ICE 376","lastStopName":"Frankfurt (Main) Hbf"},"arrivalTimestamp":32984102000,"departureTimestamp":1489746602000,"platform":"4"},
   //         "RB 17375/Waldshut/2016-02-29T17:17:00+0100":{"timestamp":1489745722324,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},"train":{"id":"RB 17375/Waldshut/2016-02-29T17:17:00+0100","category":"RB","name":"RB 17375","lastStopName":"Waldshut"},"arrivalTimestamp":32984102000,"departureTimestamp":1489746722000,"platform":"6"},
   //         "RE 5343/Basel SBB/2016-02-29T17:14:00+0100":{"timestamp":1489745722319,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},"train":{"id":"RE 5343/Basel SBB/2016-02-29T17:14:00+0100","category":"RE","name":"RE 5343","lastStopName":"Basel SBB"},"arrivalTimestamp":32984102000,"departureTimestamp":1489746542000,"delayMinute":3,"platform":""},
   //         "S 6/Zell (Wiesental)/2016-02-29T17:17:00+0100":{"timestamp":1489745722326,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},"train":{"id":"S 6/Zell (Wiesental)/2016-02-29T17:17:00+0100","category":"S","name":"S 6","lastStopName":"Zell (Wiesental)"},"arrivalTimestamp":32984102000,"departureTimestamp":1489746722000,"platform":"10"},
   //         "S 6/Basel SBB/2016-02-29T17:19:00+0100":{"timestamp":1489745722329,"stop":{"id":8500090,"name":"Basel Bad Bf","location":{"lat":47.568146,"lng":7.607283}},"train":{"id":"S 6/Basel SBB/2016-02-29T17:19:00+0100","category":"S","name":"S 6","lastStopName":"Basel SBB"},"arrivalTimestamp":32984102000,"departureTimestamp":1489746842000,"platform":""}
   //      }
   //   }
   private JsonObject getBaselBadBoard() {
      JsonObject stop = getBaselBadStop();
      return new JsonObject()
            .put("timestamp", 1489745722329L)
            .put("stop", stop)
            .put("events", new JsonArray(Arrays.asList(
                  new JsonObject().put("ICE 376/Frankfurt (Main) Hbf/2016-02-29T17:15:00+0100", new JsonObject()
                        .put("timestamp", 1489745722321L)
                        .put("stop", stop)
                        .put("train", new JsonObject()
                              .put("id", "ICE 376/Frankfurt (Main) Hbf/2016-02-29T17:15:00+0100")
                              .put("category", "ICE")
                              .put("name", "ICE 376")
                              .put("lastStopName", "Frankfurt (Main) Hbf"))
                        .put("arrivalTimestamp", 32984102000L)
                        .put("departureTimestamp", 1489746602000L)
                        .put("platform", "4")
                  ),
                  new JsonObject().put("RB 17375/Waldshut/2016-02-29T17:17:00+0100", new JsonObject()
                        .put("timestamp", 1489745722324L)
                        .put("stop", stop)
                        .put("train", new JsonObject()
                              .put("id", "RB 17375/Waldshut/2016-02-29T17:17:00+0100")
                              .put("category", "RB")
                              .put("name", "RB 17375")
                              .put("lastStopName", "Waldshut"))
                        .put("arrivalTimestamp", 32984102000L)
                        .put("departureTimestamp", 1489746722000L)
                        .put("platform", "")
                  ),
                  new JsonObject().put("RE 5343/Basel SBB/2016-02-29T17:14:00+0100", new JsonObject()
                        .put("timestamp", 1489745722319L)
                        .put("stop", stop)
                        .put("train", new JsonObject()
                              .put("id", "RE 5343/Basel SBB/2016-02-29T17:14:00+0100")
                              .put("category", "RE")
                              .put("name", "RE 5343")
                              .put("lastStopName", "Basel SBB"))
                        .put("arrivalTimestamp", 32984102000L)
                        .put("departureTimestamp", 1489746542000L)
                        .put("delayMinute", "3")
                        .put("platform", "")
                  ),
                  new JsonObject().put("S 6/Zell (Wiesental)/2016-02-29T17:17:00+0100", new JsonObject()
                        .put("timestamp", 1489745722326L)
                        .put("stop", stop)
                        .put("train", new JsonObject()
                              .put("id", "S 6/Zell (Wiesental)/2016-02-29T17:17:00+0100")
                              .put("category", "S")
                              .put("name", "S 6")
                              .put("lastStopName", "Zell (Wiesental)"))
                        .put("arrivalTimestamp", 32984102000L)
                        .put("departureTimestamp", 1489746722000L)
                        .put("platform", "10")
                  ),
                  new JsonObject().put("S 6/Basel SBB/2016-02-29T17:19:00+0100", new JsonObject()
                        .put("timestamp", 1489745722329L)
                        .put("stop", stop)
                        .put("train", new JsonObject()
                              .put("id", "S 6/Basel SBB/2016-02-29T17:19:00+0100")
                              .put("category", "S")
                              .put("name", "S 6")
                              .put("lastStopName", "Basel SBB"))
                        .put("arrivalTimestamp", 32984102000L)
                        .put("departureTimestamp", 1489746842000L)
                        .put("platform", "")
                  )
            )));
   }

   private JsonObject getBaselBadStop() {
      return new JsonObject()
               .put("id", 8500090)
               .put("name", "Basel Bad Bf")
               .put("location",
                     new JsonObject()
                           .put("lat", 47.568146)
                           .put("lng", 7.607283));
   }

}
