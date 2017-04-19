package delays.java.stream;

import static delays.java.stream.AnalyticsUtil.timed;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.json.simple.JSONObject;

import delays.java.stream.pojos.Stop;

public class AnalyticsApp {

   public static void main(String[] args) throws Exception {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());

      RemoteCache<String, Stop> remote = rcm.getCache("analytics");
      List<Map<Integer, Long>> results = timed(() ->
            remote.execute("delay-ratio", Collections.emptyMap()), "calculate delayed ratio");

      //System.out.println(results);
      storeAsJson(results);

      rcm.stop();
   }
   private static void storeAsJson(List<Map<Integer, Long>> results) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager client = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<String, String> remote = client.getCache("analytics-results");

         JSONObject json = new JSONObject();
         json.put("delayed_per_hour", perHourJson(results.get(0)));
         json.put("tot_per_hour", perHourJson(results.get(1)));

         remote.put("results", json.toJSONString());
      } finally {
         client.stop();
      }
   }

   private static JSONObject perHourJson(Map<Integer, Long> m) {
      JSONObject json = new JSONObject(m);
      return json;
   }

}
