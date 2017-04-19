package delays.utils;

import java.nio.charset.Charset;
import java.util.Objects;

public class DomainUtils {

   private static final Charset CHARSET = Charset.forName("UTF-8");

   public static byte[] bs(String s) {
      return Objects.isNull(s) ? null : s.getBytes(CHARSET);
   }

   public static String str(byte[] bs) {
      return Objects.isNull(bs) ? null : new String(bs, CHARSET);
   }

}
