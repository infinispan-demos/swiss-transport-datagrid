package delays.query.continuous;

import static delays.query.continuous.util.Protobuf.addProtoDescriptorToClient;
import static delays.query.continuous.util.Protobuf.addProtoMarshallersToClient;

import java.util.HashMap;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.api.continuous.ContinuousQuery;
import org.infinispan.query.api.continuous.ContinuousQueryListener;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import delays.query.continuous.pojos.Station;
import delays.query.continuous.pojos.StationBoard;
import delays.query.continuous.pojos.Stop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class ContinuousQueryVerticle extends AbstractVerticle {

   private RemoteCacheManager client;
   private RemoteCache<Station, StationBoard> stationBoards;
   private ContinuousQuery<Station, StationBoard> continuousQuery;

   @Override
   public void start() throws Exception {
      System.out.println("Start continuous query verticle");

      client = new RemoteCacheManager();
      System.out.println("Started remote cache manager");

      addProtoDescriptorToClient(client);
      addProtoMarshallersToClient(client);

      stationBoards = client.getCache("default");
      QueryFactory qf = Search.getQueryFactory(stationBoards);

      // TODO: Demo Event Broker - 1.1 - Implement Query
      Query query = null;

      ContinuousQueryListener<Station, StationBoard> listener =
            new ContinuousQueryListener<Station, StationBoard>() {
               @Override
               public void resultJoining(Station key, StationBoard value) {
                  // TODO: Demo Event Broker - 1.2 - Push delayed stops
               }

               @Override
               public void resultUpdated(Station key, StationBoard value) {
               }

               @Override
               public void resultLeaving(Station key) {
               }
            };

      continuousQuery = Search.getContinuousQuery(stationBoards);

      // TODO: Demo Event Broker - 1.3 - Add continuous query
   }

   private void publishDelay(Station key, Stop e) {
      vertx.runOnContext(x -> {
         System.out.println(e);
         vertx.eventBus().publish("delays", toJson(key, e));
      });
   }

   private String toJson(Station station, Stop stop) {
      Map<String, Object> map = new HashMap<>();
      map.put("type", stop.train.cat);
      map.put("departure", String.format("%tR", stop.departureTs));
      map.put("station", station.name);
      map.put("destination", stop.train.to);
      map.put("delay", stop.delayMin);
      map.put("trainName", stop.train.name);
      return new JsonObject(map).encode();
   }

   @Override
   public void stop() throws Exception {
      if (continuousQuery != null)
         continuousQuery.removeAllListeners();

      if (stationBoards != null)
         stationBoards.clear();

      if (client != null)
         client.stop();
   }

}
