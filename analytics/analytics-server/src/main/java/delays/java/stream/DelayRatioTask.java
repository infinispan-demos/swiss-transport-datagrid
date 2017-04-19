package delays.java.stream;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.stream.CacheCollectors;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;

import delays.java.stream.pojos.Stop;

public class DelayRatioTask implements ServerTask {

   private TaskContext ctx;

   @Override
   public void setTaskContext(TaskContext ctx) {
      this.ctx = ctx;
   }

   @Override
   public String getName() {
      return "delay-ratio";
   }

   @Override
   public Object call() throws Exception {
      System.out.println("Execute delay-ratio task");

      Cache<String, Stop> cache = getCache();

      Map<Integer, Long> totalPerHour = cache.values().stream().collect(
            CacheCollectors.serializableCollector(() -> Collectors.groupingBy(
                  e -> {
                     Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"), Locale.ENGLISH);
                     c.setTime(e.departureTs);
                     return c.get(Calendar.HOUR_OF_DAY);
                  },
                  TreeMap::new,
                  Collectors.counting()
            )));

      Map<Integer, Long> delayedPerHour = cache.values().stream()
            .filter(e -> e.delayMin > 0)
            .collect(
                  CacheCollectors.serializableCollector(() -> Collectors.groupingBy(
                        e -> {
                           Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"), Locale.ENGLISH);
                           c.setTime(e.departureTs);
                           return c.get(Calendar.HOUR_OF_DAY);
                        },
                        TreeMap::new,
                        Collectors.counting()
                  )));

      return Arrays.asList(delayedPerHour, totalPerHour);
   }

   @Override
   public TaskExecutionMode getExecutionMode() {
      return TaskExecutionMode.ONE_NODE;
   }

   @SuppressWarnings("unchecked")
   private <K, V> Cache<K, V> getCache() {
      return (Cache<K, V>) ctx.getCache().get();
   }

//   private static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(
//         Function<? super T, ? extends K> classifier,
//         Collector<? super T, A, D> downstream) {
//      return CacheCollectors.serializableCollector(
//            () -> Collectors.groupingBy(classifier, downstream));
//   }

}
