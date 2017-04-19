package delays.java.stream.pojos;

import static delays.utils.DomainUtils.bs;
import static delays.utils.DomainUtils.str;

import java.io.IOException;
import java.io.Serializable;

import org.infinispan.protostream.MessageMarshaller;

public class Train implements Serializable {

//   private String id;
   private final byte[] name;
   private final byte[] to;
   private final byte[] cat;
   private final byte[] operator;

   private Train(byte[] name, byte[] to, byte[] cat, byte[] operator) {
      this.name = name;
      this.to = to;
      this.cat = cat;
      this.operator = operator;
   }

   public static Train make(String name, String to, String cat, String operator) {
      return new Train(bs(name), bs(to), bs(cat), bs(operator));
   }

   public String getName() {
      return str(name);
   }

   public String getTo() {
      return str(to);
   }

   public String getCat() {
      return str(cat);
   }

   public String getOperator() {
      return str(operator);
   }

   @Override
   public String toString() {
      return "TrainAnalytics{" +
            "name='" + getName() + '\'' +
            ", to='" + getTo() + '\'' +
            ", cat='" + getCat() + '\'' +
            ", operator='" + getOperator() + '\'' +
            '}';
   }

   public static final class Marshaller implements MessageMarshaller<Train> {

      @Override
      public Train readFrom(ProtoStreamReader reader) throws IOException {
         byte[] name = reader.readBytes("name");
         byte[] to = reader.readBytes("to");
         byte[] cat = reader.readBytes("cat");
         byte[] operator = reader.readBytes("operator");
         return new Train(name, to, cat, operator);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Train obj) throws IOException {
         writer.writeBytes("name", obj.name);
         writer.writeBytes("to", obj.to);
         writer.writeBytes("cat", obj.cat);
         writer.writeBytes("operator", obj.operator);
      }

      @Override
      public Class<? extends Train> getJavaClass() {
         return Train.class;
      }

      @Override
      public String getTypeName() {
         return "analytics.Train";
      }

   }

}
