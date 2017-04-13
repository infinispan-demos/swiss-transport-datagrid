package delays.query.continuous.pojos;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

public class GeoLoc {

   public final double lat;
   public final double lng;

   public GeoLoc(double lat, double lng) {
      this.lat = lat;
      this.lng = lng;
   }

   @Override
   public String toString() {
      return "GeoLoc{" +
            "lat=" + lat +
            ", lng=" + lng +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      GeoLoc geoLoc = (GeoLoc) o;

      if (Double.compare(geoLoc.lat, lat) != 0) return false;
      return Double.compare(geoLoc.lng, lng) == 0;
   }

   @Override
   public int hashCode() {
      int result;
      long temp;
      temp = Double.doubleToLongBits(lat);
      result = (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(lng);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<GeoLoc> {

      @Override
      public GeoLoc readFrom(ProtoStreamReader reader) throws IOException {
         Double lat = reader.readDouble("lat");
         Double lng = reader.readDouble("lng");
         return new GeoLoc(lat, lng);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, GeoLoc geoLoc) throws IOException {
         writer.writeDouble("lat", geoLoc.lat);
         writer.writeDouble("lng", geoLoc.lng);
      }

      @Override
      public Class<? extends GeoLoc> getJavaClass() {
         return GeoLoc.class;
      }

      @Override
      public String getTypeName() {
         return "real_time.Station.GeoLoc";
      }

   }

}
