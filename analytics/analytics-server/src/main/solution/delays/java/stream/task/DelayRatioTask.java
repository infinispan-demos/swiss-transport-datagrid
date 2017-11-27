package delays.java.stream.task;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.commons.dataconversion.IdentityEncoder;
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

      Map<Integer, Long> totalPerHour = cache.values().stream()
         .collect(() -> Collectors.groupingBy(
            e -> getHourOfDay(e.departureTs),
            Collectors.counting()
         ));

      Map<Integer, Long> delayedPerHour = cache.values().stream()
         .filter(e -> e.delayMin > 0)
         .collect(() -> Collectors.groupingBy(
            e -> getHourOfDay(e.departureTs),
            Collectors.counting()
         ));

      return Arrays.asList(delayedPerHour, totalPerHour);
   }

   @Override
   public TaskExecutionMode getExecutionMode() {
      return TaskExecutionMode.ONE_NODE;
   }

   @SuppressWarnings("unchecked")
   private <K, V> Cache<K, V> getCache() {
      return (Cache<K, V>) ctx.getCache().get().getAdvancedCache()
         .withEncoding(IdentityEncoder.class);
   }

   private static int getHourOfDay(Date date) {
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"), Locale.ENGLISH);
      c.setTime(date);
      return c.get(Calendar.HOUR_OF_DAY);
   }

}
