package delays.query.continuous.pojos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.infinispan.protostream.MessageMarshaller;

public class StationBoard {

   public final Date ts;
   public final List<Stop> entries;

   public StationBoard(Date timestamp, List<Stop> entries) {
      this.ts = timestamp;
      this.entries = entries;
   }

   public Date getTs() {
      return ts;
   }

   public List<Stop> getEntries() {
      return entries;
   }

   @Override
   public String toString() {
      return "StationBoard{" +
            "ts=" + ts +
            ", entries=" + entries +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      StationBoard that = (StationBoard) o;

      if (!ts.equals(that.ts)) return false;
      return entries.equals(that.entries);
   }

   @Override
   public int hashCode() {
      int result = ts.hashCode();
      result = 31 * result + entries.hashCode();
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<StationBoard> {

      @Override
      public StationBoard readFrom(ProtoStreamReader reader) throws IOException {
         Date ts = reader.readDate("ts");
         List<Stop> entries = reader
               .readCollection("entries", new ArrayList<>(), Stop.class);
         return new StationBoard(ts, entries);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, StationBoard stationBoard) throws IOException {
         writer.writeDate("ts", stationBoard.ts);
         writer.writeCollection("entries", stationBoard.entries, Stop.class);
      }

      @Override
      public Class<? extends StationBoard> getJavaClass() {
         return StationBoard.class;
      }

      @Override
      public String getTypeName() {
         return "real_time.StationBoard";
      }

   }

}
