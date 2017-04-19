package delays.java.stream;

import static delays.java.stream.AnalyticsUtil.timed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.RemoteCache;

import delays.java.stream.pojos.Stop;
import delays.java.stream.pojos.Station;
import delays.java.stream.pojos.Train;
import delays.java.stream.util.Gzip;

public class Injector {

   private static final String GZIP_FILE_NAME = "src/main/resources/station-boards-dump-3_weeks.tsv.gz";
   private static final String GZIP_TARGET_FILE_NAME = String.format(
         "%s/station-boards-dump-3_weeks.tsv",
         System.getProperty("java.io.tmpdir"));

   static Calendar calendar = null;
   static String lastDate = "?";

   public static void inject(RemoteCache<String, Stop> cache) throws Exception {
      Path gunzipped = Gzip.gunzip(new File(GZIP_FILE_NAME), new File(GZIP_TARGET_FILE_NAME));
      Map<String, Stop> entries =
            timed(() -> loadEntries(gunzipped), "read and split lines");

      timed(() -> storeCache(cache, entries), "store remotely");
   }

   private static void storeCache(
         RemoteCache<String, Stop> cache,
         Map<String, Stop> entries) {
      int total = 0;
      int max = 1 << 14; // 2 ^ ...
      Map<String, Stop> tmp = new HashMap<>(max);
      int count = 0;
      for (Map.Entry<String, Stop> entry : entries.entrySet()) {
         if (count > max) {
            cache.putAll(tmp);
            tmp = new HashMap<>(max);
            total += max;
            count = 0;
            System.out.println("Stored " + total);
         }

         tmp.put(entry.getKey(), entry.getValue());
         count++;
      }
   }

   private static Map<String, Stop> loadEntries(Path gunzipped) {
      Map<String, Stop> entries = new HashMap<>();
      try (Stream<String> lines = lines(gunzipped)) {
         lines.skip(1) // Skip header
            .map(l -> toEntry(l))
            .forEach(e -> entries.put((String) e[0], (Stop) e[1]));
      }
      return entries;
   }

   // TODO: Move to launderer
   private static Stream<String> lines(Path gunzipped) {
      try {
         return Files.lines(gunzipped);
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

   private static Object[] toEntry(String l) {
      String[] parts = l.split("\t");

      String id = parts[0];
      //Date entryTs = parseTimestamp(parts[1]);
      //Date entryTs = null;
      long stopId = Long.parseLong(parts[2]);
      String stopName = parts[3];
      Date departureTs = parseTimestamp(parts[4]);
      String trainName = parts[5];
      String trainCat = parts[6];
      String trainOperator = parts[7];
      String trainTo = parts[8];
      int delayMin = parts[9].isEmpty() ? 0 : Integer.parseInt(parts[9]);
      String capacity1st = parts[10];
      String capacity2nd = parts[11];

      Train train = Train.make(trainName, trainTo, trainCat, trainOperator);
      Station station = Station.make(stopId, stopName);
      Stop entry = Stop.make(
            train, departureTs, null, null, delayMin, station, null, capacity1st, capacity2nd);

      return new Object[]{id, entry};
   }

   private static Date parseTimestamp(String date) {
      if (!date.startsWith(lastDate)) {
         //System.out.println("New date!");
         Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"), Locale.ENGLISH);
         int year = Integer.parseInt(date.substring(11, 15));
         int month = toMonth(date.substring(4, 7));
         int day = Integer.parseInt(date.substring(8, 10));
//         System.out.println("Year: " + year);
//         System.out.println("Month: " + month);
//         System.out.println("Day: " + day);
         c.set(year, month, day, 0, 0, 0);
         calendar = c;
         lastDate = date.substring(0, 15);
      }

      String hour = date.substring(16, 18);
      String minute = date.substring(19, 21);
//      System.out.println("Hour: " + hour);
//      System.out.println("Minute: " + minute);
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
      calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
//      System.out.println(calendar.getTime());
      return calendar.getTime();
   }

   private static int toMonth(String m) {
      switch (m) {
         case "Jan": return Calendar.JANUARY;
         case "Feb": return Calendar.FEBRUARY;
         case "Mar": return Calendar.MARCH;
         case "Apr": return Calendar.APRIL;
         case "May": return Calendar.MAY;
         case "Jun": return Calendar.JUNE;
         case "Jul": return Calendar.JULY;
         case "Aug": return Calendar.AUGUST;
         case "Sep": return Calendar.SEPTEMBER;
         case "Oct": return Calendar.OCTOBER;
         case "Nov": return Calendar.NOVEMBER;
         case "Dec": return Calendar.DECEMBER;
         default:
            throw new IllegalArgumentException("Unknown month: `" + m + "`");
      }
   }

}
