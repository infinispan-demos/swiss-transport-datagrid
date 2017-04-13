package delays.query.continuous;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.io.IOException;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.util.Util;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class RealTimeVerticle extends AbstractVerticle {

   private RemoteCacheManager client;

   @Override
   public void start() throws Exception {
      System.out.println("Start realt-time verticle");
      client = new RemoteCacheManager();
      System.out.println("Started remote cache manager");

      addProtoDescriptorToServer(client.getCache(PROTOBUF_METADATA_CACHE_NAME));
      deployInjectorVerticle();
      deployContinuousQueryVerticle();
   }

   @Override
   public void stop() throws Exception {
      if (client != null)
         client.stop();
   }

   private void deployInjectorVerticle() {
      DeploymentOptions options = new DeploymentOptions().setWorker(true);
      vertx.deployVerticle("delays.query.continuous.InjectorVerticle", options);
   }

   private void deployContinuousQueryVerticle() {
      DeploymentOptions options = new DeploymentOptions().setWorker(true);
      vertx.deployVerticle("delays.query.continuous.ContinuousQueryVerticle", options);
   }

   private void addProtoDescriptorToServer(RemoteCache<String, String> metaCache) throws IOException {
      metaCache.put("real-time.proto", Util.read(getClass().getResourceAsStream("/real-time.proto")));
      String errors = metaCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
      if (errors != null)
         throw new AssertionError("Error in proto file");
   }

}
