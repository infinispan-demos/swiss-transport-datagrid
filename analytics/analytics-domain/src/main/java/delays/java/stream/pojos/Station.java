package delays.java.stream.pojos;

import static delays.utils.DomainUtils.bs;
import static delays.utils.DomainUtils.str;

import java.io.IOException;
import java.io.Serializable;

import org.infinispan.protostream.MessageMarshaller;

public class Station implements Serializable {

   public final long id;
   private final byte[] name;
//   private GeoLoc loc;

   private Station(long id, byte[] name) {
      this.id = id;
      this.name = name;
   }

   public static Station make(long id, String name) {
      return new Station(id, bs(name));
   }

   public String getName() {
      return str(name);
   }

   @Override
   public String toString() {
      return "Station{" +
            "id=" + id +
            ", name='" + getName() + '\'' +
            '}';
   }

   public static final class Marshaller implements MessageMarshaller<Station> {

      @Override
      public Station readFrom(ProtoStreamReader reader) throws IOException {
         long id = reader.readLong("id");
         byte[] name = reader.readBytes("name");
         return new Station(id, name);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Station obj) throws IOException {
         writer.writeLong("id", obj.id);
         writer.writeBytes("name", obj.name);
      }

      @Override
      public Class<? extends Station> getJavaClass() {
         return Station.class;
      }

      @Override
      public String getTypeName() {
         return "analytics.Station";
      }

   }


}
