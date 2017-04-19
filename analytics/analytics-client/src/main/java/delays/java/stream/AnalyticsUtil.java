package delays.java.stream;

import static java.lang.System.out;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AnalyticsUtil {

   static <T> T timed(Supplier<T> f, String msg) {
      final long start = System.nanoTime();
      try {
         return f.get();
      } catch (Exception e) {
         throw new AssertionError(e);
      } finally {
         final long duration = System.nanoTime() - start;
         out.printf("duration %d(s) %s %n",
               TimeUnit.NANOSECONDS.toSeconds(duration), msg);
      }
   }

   static void timed(Runnable r, String msg) {
      final long start = System.nanoTime();
      try {
         r.run();
      } catch (Exception e) {
         throw new AssertionError(e);
      } finally {
         final long duration = System.nanoTime() - start;
         out.printf("duration %d(s) %s %n",
               TimeUnit.NANOSECONDS.toSeconds(duration), msg);
      }
   }

}
