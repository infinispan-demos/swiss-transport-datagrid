package delays.java.stream;

import java.util.Objects;
import java.util.function.Function;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;

final class RemoteDataGrid {

   private static final String SERVER_HOST = System.getProperty("server.host", "datagrid");
   private static final int SERVER_PORT = Integer.getInteger("server.port", 11222);

   static Supplier<ConfigurationBuilder> config() {
      ConfigurationBuilder cfg = new ConfigurationBuilder();
      cfg.addServer().host(SERVER_HOST).port(SERVER_PORT);
      return () -> cfg;
   }

   static Function<ConfigurationBuilder, ConfigurationBuilder> protostream() {
      return cfg -> cfg.marshaller((new ProtoStreamMarshaller()));
   }

   private RemoteDataGrid() {
   }

   interface Supplier<T> extends java.util.function.Supplier<T> {
      default <V> Supplier<V> andThen(Function<? super T, ? extends V> after) {
         Objects.requireNonNull(after);
         return () -> after.apply(get());
      }
   }

}
