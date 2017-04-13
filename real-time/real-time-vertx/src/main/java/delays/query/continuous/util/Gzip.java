package delays.query.continuous.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

// TODO: Duplicate with analytics <- refactor when refactoring domain
public class Gzip {

   public static Path gunzip(InputStream from, File to) throws Exception {
      if (!to.exists()) {
         byte[] buffer = new byte[1024];
         GZIPInputStream gzis = new GZIPInputStream(from);
         FileOutputStream out = new FileOutputStream(to);

         int len;
         while ((len = gzis.read(buffer)) > 0)
            out.write(buffer, 0, len);

         gzis.close();
         out.close();

         System.out.printf("Finished gunzip to: %s%n", to);
      } else {
         System.out.printf("File already gunzipped to: %s%n", to);
      }

      return to.toPath();
   }

}
