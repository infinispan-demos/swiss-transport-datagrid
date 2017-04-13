package delays.query.continuous;

import static delays.query.continuous.util.Protobuf.addProtoDescriptorToClient;
import static delays.query.continuous.util.Protobuf.addProtoMarshallersToClient;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.api.continuous.ContinuousQuery;
import org.infinispan.query.api.continuous.ContinuousQueryListener;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import delays.query.continuous.pojos.Station;
import delays.query.continuous.pojos.StationBoard;
import io.vertx.core.AbstractVerticle;

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
      Query query = qf.from(StationBoard.class)
            .having("entries.delayMin").gt(0L)
            .build();

      ContinuousQueryListener<Station, StationBoard> listener =
            new ContinuousQueryListener<Station, StationBoard>() {
               @Override
               public void resultJoining(Station key, StationBoard value) {
                  value.entries.stream()
                        .filter(e -> e.delayMin > 0)
                        .forEach(e -> {
                           System.out.println(e);
//                           queue.add(new StationBoardView(
//                                 e.train.cat,
//                                 String.format("%tR", e.departureTs),
//                                 key.name,
//                                 e.train.to,
//                                 "+" + e.delayMin,
//                                 e.train.name
//                           ));
                        });
               }

               @Override
               public void resultUpdated(Station key, StationBoard value) {
               }

               @Override
               public void resultLeaving(Station key) {
               }
            };

      continuousQuery = Search.getContinuousQuery(stationBoards);
      continuousQuery.addContinuousQueryListener(query, listener);
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
