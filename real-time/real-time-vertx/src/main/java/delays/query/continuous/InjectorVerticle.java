package delays.query.continuous;

import static delays.query.continuous.util.Protobuf.addProtoDescriptorToClient;
import static delays.query.continuous.util.Protobuf.addProtoMarshallersToClient;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

import delays.query.continuous.pojos.Station;
import delays.query.continuous.pojos.StationBoard;
import io.vertx.core.AbstractVerticle;

public class InjectorVerticle extends AbstractVerticle {

   private final AtomicBoolean stopped = new AtomicBoolean(false);
   private Future<Void> injectFuture;

   private RemoteCacheManager client;
   private RemoteCache<Station, StationBoard> stationBoards;

   @Override
   public void start() throws Exception {
      System.out.println("Start injector verticle");
      client = new RemoteCacheManager();
      System.out.println("Started remote cache manager");

      addProtoDescriptorToClient(client);
      addProtoMarshallersToClient(client);

      stationBoards = client.getCache("default");
      stationBoards.clear();
      injectFuture = Injector.submitCycle(stationBoards, stopped);
   }

   @Override
   public void stop() throws Exception {
      stopped.set(true);

      if (injectFuture != null)
         injectFuture.cancel(true);

      if (stationBoards != null)
         stationBoards.clear();;

      if (client != null)
         client.stop();
   }

}
