package delays.query.continuous.util;

public class Util {

   public static <T> T s(NoisySupplier<T> s) {
      try {
         return s.get();
      } catch (Exception e) {
         throw new AssertionError(e);
      }
   }

   public static void r(NoisyRunnable r) {
      try {
         r.run();
      } catch (Exception e) {
         throw new AssertionError(e);
      }
   }

   public interface NoisySupplier<T> {
      T get() throws Exception;
   }

   public interface NoisyRunnable {
      void run() throws Exception;
   }

   interface NoisyFunction<T, R> {
      R apply(T t) throws Exception;
   }

}
