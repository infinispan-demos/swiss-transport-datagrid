package delays.query.continuous.util;

import java.io.IOException;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;

import delays.query.continuous.pojos.GeoLoc;
import delays.query.continuous.pojos.Station;
import delays.query.continuous.pojos.StationBoard;
import delays.query.continuous.pojos.Stop;
import delays.query.continuous.pojos.Train;

public class Protobuf {

   private Protobuf() {
   }

   public static SerializationContext addProtoDescriptorToClient(RemoteCacheManager client) throws IOException {
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(client);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources("real-time.proto"));
      return ctx;
   }

   public static void addProtoMarshallersToClient(RemoteCacheManager client) throws IOException {
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(client);
      ctx.registerMarshaller(new GeoLoc.Marshaller());
      ctx.registerMarshaller(new StationBoard.Marshaller());
      ctx.registerMarshaller(new Stop.Marshaller());
      ctx.registerMarshaller(new Station.Marshaller());
      ctx.registerMarshaller(new Train.Marshaller());
   }

}
