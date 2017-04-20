package delays.java.stream;

import static java.lang.System.out;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AnalyticsUtil {

   static <T> T timed(Supplier<T> f, String msg) {
      final long start = System.nanoTime();
      try {
         out.printf("Start: %s%n", msg);
         return f.get();
      } catch (Exception e) {
         throw new AssertionError(e);
      } finally {
         final long duration = System.nanoTime() - start;
         out.printf("Duration: %d(s) %s %n",
               TimeUnit.NANOSECONDS.toSeconds(duration), msg);
      }
   }

   static void timed(Runnable r, String msg) {
      final long start = System.nanoTime();
      try {
         out.printf("Start: %s%n", msg);
         r.run();
      } catch (Exception e) {
         throw new AssertionError(e);
      } finally {
         final long duration = System.nanoTime() - start;
         out.printf("Duration: %d(s) %s %n",
               TimeUnit.NANOSECONDS.toSeconds(duration), msg);
      }
   }

}
