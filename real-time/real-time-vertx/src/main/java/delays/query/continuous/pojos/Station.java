package delays.query.continuous.pojos;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

public class Station {

   public final long id;
   public final String name;
   public final GeoLoc loc;

   public Station(long id, String name, GeoLoc loc) {
      this.id = id;
      this.name = name;
      this.loc = loc;
   }

   @Override
   public String toString() {
      return "Stop{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", loc=" + loc +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Station station = (Station) o;

      if (id != station.id) return false;
      if (!name.equals(station.name)) return false;
      return loc.equals(station.loc);
   }

   @Override
   public int hashCode() {
      int result = (int) (id ^ (id >>> 32));
      result = 31 * result + name.hashCode();
      result = 31 * result + loc.hashCode();
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<Station> {

      @Override
      public Station readFrom(ProtoStreamReader reader) throws IOException {
         long id = reader.readLong("id");
         String name = reader.readString("name");
         GeoLoc location = reader.readObject("loc", GeoLoc.class);
         return new Station(id, name, location);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Station station) throws IOException {
         writer.writeLong("id", station.id);
         writer.writeString("name", station.name);
         writer.writeObject("loc", station.loc, GeoLoc.class);
      }

      @Override
      public Class<? extends Station> getJavaClass() {
         return Station.class;
      }

      @Override
      public String getTypeName() {
         return "real_time.Station";
      }

   }

}
