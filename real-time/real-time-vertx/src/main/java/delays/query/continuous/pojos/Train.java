package delays.query.continuous.pojos;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

public class Train {

   public final String id;
   public final String name;
   public final String to;
   public final String cat;

   public Train(String id, String name, String to, String cat) {
      this.id = id;
      this.name = name;
      this.to = to;
      this.cat = cat;
   }

   @Override
   public String toString() {
      return "Train{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", to='" + to + '\'' +
            ", cat='" + cat + '\'' +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Train train = (Train) o;

      if (!id.equals(train.id)) return false;
      if (!name.equals(train.name)) return false;
      if (!to.equals(train.to)) return false;
      return cat.equals(train.cat);
   }

   @Override
   public int hashCode() {
      int result = id.hashCode();
      result = 31 * result + name.hashCode();
      result = 31 * result + to.hashCode();
      result = 31 * result + cat.hashCode();
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<Train> {

      @Override
      public Train readFrom(ProtoStreamReader reader) throws IOException {
         String id = reader.readString("id");
         String name = reader.readString("name");
         String to = reader.readString("to");
         String cat = reader.readString("cat");
         return new Train(id, name, to, cat);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Train train) throws IOException {
         writer.writeString("id", train.id);
         writer.writeString("name", train.name);
         writer.writeString("to", train.to);
         writer.writeString("cat", train.cat);
      }

      @Override
      public Class<? extends Train> getJavaClass() {
         return Train.class;
      }

      @Override
      public String getTypeName() {
         return "real_time.Train";
      }

   }

}
