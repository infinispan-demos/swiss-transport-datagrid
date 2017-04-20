package delays.java.stream;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;

import delays.java.stream.pojos.Station;
import delays.java.stream.pojos.Stop;
import delays.java.stream.pojos.Train;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class InjectorVerticle extends AbstractVerticle {

   private RemoteCacheManager client;
   private RemoteCache<String, Stop> stops;

   @Override
   public void start(Future<Void> future) throws Exception {
      System.out.println("Start analytics injector verticle");

      Configuration cfg =
            RemoteDataGrid.config()
                  .andThen(RemoteDataGrid.protostream())
                  .get().build();
      client = new RemoteCacheManager(cfg);

      System.out.println("Started remote cache manager");

      stops = client.getCache("analytics");
      stops.clear();

      addProtoDescriptorToServer(client.getCache(PROTOBUF_METADATA_CACHE_NAME));
      addProtoMarshallersToServer(); // Required for server tasks

      addProtoDescriptorToClient(client);
      addProtoMarshallersToClient(client);

      Injector.submitData(stops, future);
   }

   private static void addProtoMarshallersToServer() {
      Configuration cfg = RemoteDataGrid.config().get().build();
      RemoteCacheManager rcm = new RemoteCacheManager(cfg);
      try {
         RemoteCache<String, Stop> remote = rcm.getCache("analytics");
         remote.execute("add-protobuf", Collections.emptyMap());
      } finally {
         rcm.stop();
      }
   }

   private static void addProtoDescriptorToServer(RemoteCache<String, String> metaCache) throws IOException {
      metaCache.put("analytics.proto", read(AnalyticsVerticle.class.getResourceAsStream("/analytics.proto")));
      String errors = metaCache.get(".errors");
      if (errors != null)
         throw new AssertionError("Errors found in proto file: " + errors);
   }

   private static void addProtoMarshallersToClient(RemoteCacheManager client) throws IOException {
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(client);
      ctx.registerMarshaller(new Stop.Marshaller());
      ctx.registerMarshaller(new Station.Marshaller());
      ctx.registerMarshaller(new Train.Marshaller());
   }

   private static SerializationContext addProtoDescriptorToClient(RemoteCacheManager client) throws IOException {
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(client);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources("analytics.proto"));
      return ctx;
   }

   /**
    * Reads the given InputStream fully, closes the stream and returns the result as a String.
    *
    * @param is the stream to read
    * @return the UTF-8 string
    * @throws java.io.IOException in case of stream read errors
    */
   private static String read(InputStream is) throws IOException {
      try {
         final Reader reader = new InputStreamReader(is, "UTF-8");
         StringWriter writer = new StringWriter();
         char[] buf = new char[1024];
         int len;
         while ((len = reader.read(buf)) != -1) {
            writer.write(buf, 0, len);
         }
         return writer.toString();
      } finally {
         is.close();
      }
   }

   @Override
   public void stop() throws Exception {
      if (stops != null)
         stops.clear();

      if (client != null)
         client.stop();
   }

}
