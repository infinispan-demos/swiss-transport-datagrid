package delays.query.continuous.pojos;

import java.io.IOException;
import java.util.Date;

import org.infinispan.protostream.MessageMarshaller;

public class Stop {

   public final Train train;
   public final Date departureTs;
   public final String platform;
   public final Long delayMin;

   private Date arrivalTs; // nullable

   public Stop(Train train, Date departureTs, String platform, Date arrivalTs, Long delayMin) {
      this.train = train;
      this.departureTs = departureTs;
      this.platform = platform;
      this.arrivalTs = arrivalTs;
      this.delayMin = delayMin;
   }

   @Override
   public String toString() {
      return "Stop{" +
            "train=" + train +
            ", departureTs=" + departureTs +
            ", platform='" + platform + '\'' +
            ", arrivalTs=" + arrivalTs +
            ", delayMin=" + delayMin +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Stop that = (Stop) o;

      if (!train.equals(that.train)) return false;
      if (!departureTs.equals(that.departureTs)) return false;
      if (!platform.equals(that.platform)) return false;
      if (!delayMin.equals(that.delayMin)) return false;
      return arrivalTs != null ? arrivalTs.equals(that.arrivalTs) : that.arrivalTs == null;
   }

   @Override
   public int hashCode() {
      int result = train.hashCode();
      result = 31 * result + departureTs.hashCode();
      result = 31 * result + platform.hashCode();
      result = 31 * result + delayMin.hashCode();
      result = 31 * result + (arrivalTs != null ? arrivalTs.hashCode() : 0);
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<Stop> {

      @Override
      public Stop readFrom(ProtoStreamReader reader) throws IOException {
         Train train = reader.readObject("train", Train.class);
         Date departureTs = reader.readDate("departureTs");
         String platform = reader.readString("platform");
         Date arrivalTs = reader.readDate("arrivalTs");
         Long delayMin = reader.readLong("delayMin");
         return new Stop(train, departureTs, platform, arrivalTs, delayMin);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Stop entry) throws IOException {
         writer.writeObject("train", entry.train, Train.class);
         writer.writeDate("departureTs", entry.departureTs);
         writer.writeString("platform", entry.platform);
         writer.writeDate("arrivalTs", entry.arrivalTs);
         writer.writeLong("delayMin", entry.delayMin);
      }

      @Override
      public Class<? extends Stop> getJavaClass() {
         return Stop.class;
      }

      @Override
      public String getTypeName() {
         return "real_time.Stop";
      }

   }

}
