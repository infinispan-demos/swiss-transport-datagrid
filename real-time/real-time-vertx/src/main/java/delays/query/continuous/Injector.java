package delays.query.continuous;

import static delays.query.continuous.util.Util.r;
import static delays.query.continuous.util.Util.s;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.RemoteCache;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import delays.query.continuous.pojos.GeoLoc;
import delays.query.continuous.pojos.Station;
import delays.query.continuous.pojos.StationBoard;
import delays.query.continuous.pojos.Stop;
import delays.query.continuous.pojos.Train;
import delays.query.continuous.util.Gzip;

public class Injector {

   static final String FILE_NAME = "src/main/resources/stationboard-sample.jsonl";

   static final String GZIP_FILE_NAME = "/cff-stop-2016-02-29__.jsonl.gz";
   static final String GZIP_TARGET_FILE_NAME = String.format(
         "%s/cff-stop-2016-02-29__.jsonl",
         System.getProperty("java.io.tmpdir"));

   static final int SPEEDUP = 10;

   static Map.Entry<Station, StationBoard> headStationBoard() throws IOException {
      try (Stream<String> lines = Files.lines(Paths.get(FILE_NAME))) {
         String entry = lines.findFirst().get();

         JSONParser parser = new JSONParser();
         JSONObject json = (JSONObject) s(() -> parser.parse(entry));

         JSONObject jsonStop = (JSONObject) json.get("stop");
         JSONObject jsonSt = (JSONObject) jsonStop.get("station");

         Station station = mkStop(jsonSt);

         Date ts = new Date((long) json.get("timeStamp"));
         Stop boardEntry = mkStationBoardEntry(json, jsonStop);
         StationBoard board = new StationBoard(ts, Arrays.asList(boardEntry));

         Map<Station, StationBoard> map = new HashMap<>();
         map.put(station, board);
         return map.entrySet().iterator().next();
      }
   }

   static Station prevStation = null;
   static Date prevTs = null;

   public static Future<Void> submitCycle(
         RemoteCache<Station, StationBoard> boards, AtomicBoolean stopped) throws Exception {
      return Executors.newSingleThreadExecutor().submit(() -> {
         try {
            System.out.println("Cycle...");
            cycle(boards, stopped);
            return null;
         } catch (Throwable t) {
            t.printStackTrace();
            return null;
         }
      });
   }

   public static void cycle(
         RemoteCache<Station, StationBoard> boards, AtomicBoolean stopped) throws Exception {
      Path gunzipped = Gzip.gunzip(
            Injector.class.getResourceAsStream(GZIP_FILE_NAME),
            new File(GZIP_TARGET_FILE_NAME));
      try (Stream<String> lines = Files.lines(gunzipped)) {
         JSONParser parser = new JSONParser();
         List<Stop> boardEntries = new ArrayList<>();
         lines.forEach(l -> {
            if (stopped.get())
               return;

            JSONObject json = (JSONObject) s(() -> parser.parse(l));
            JSONObject jsonStop = (JSONObject) json.get("stop");
            JSONObject jsonSt = (JSONObject) jsonStop.get("station");

            Station station = mkStop(jsonSt);
            Date ts = new Date((long) json.get("timeStamp"));
            Stop boardEntry = mkStationBoardEntry(json, jsonStop);

            if (prevStation == null)
               prevStation = station;

            if (prevTs == null)
               prevTs = ts;

            if (prevStation.equals(station) && prevTs.equals(ts)) {
               boardEntries.add(boardEntry);
            } else {
               long diff = dateDiff(prevTs, ts, TimeUnit.MILLISECONDS);
               if (diff > 0)
                  r(() -> Thread.sleep(diff / SPEEDUP));

               //System.out.println("Put: " + prevStation + ", with: " + new StationBoard(prevTs, boardEntries));
               boards.put(prevStation, new StationBoard(prevTs, boardEntries));
               boardEntries.clear();
               boardEntries.add(boardEntry);
               prevStation = station;
               prevTs = ts;
            }
         });
         // Store last board
         boards.put(prevStation, new StationBoard(prevTs, boardEntries));
      }
   }

   private static long dateDiff(Date date1, Date date2, TimeUnit timeUnit) {
      long diffInMillies = date2.getTime() - date1.getTime();
      return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
   }

   private static Stop mkStationBoardEntry(JSONObject json, JSONObject jsonStop) {
      Train train = mkTrain(json, jsonStop);
      Date departureTs = new Date((long) jsonStop.get("departureTimestamp") * 1000);
      String platform = (String) jsonStop.get("platform");
      Object arrivalSt = jsonStop.get("arrivalTimestamp");
      Object delayMin = jsonStop.get("delay");
      return new Stop(
            train, departureTs, platform, orNull(arrivalSt), orNull(delayMin, 0L));
   }

   @SuppressWarnings("unchecked")
   private static <T> T orNull(Object obj) {
      return Objects.isNull(obj) ? null : (T) obj;
   }

   @SuppressWarnings("unchecked")
   private static <T> T orNull(Object obj, T defaultValue) {
      return Objects.isNull(obj) ? defaultValue : (T) obj;
   }

   private static Train mkTrain(JSONObject json, JSONObject jsonStop) {
      String trName = (String) json.get("name");
      String to = (String) json.get("to");
      String departure = (String) jsonStop.get("departure");
      String id = String.format("%s/%s/%s", trName, to, departure);
      String cat = (String) json.get("category");
      return new Train(id, trName, to, cat);
   }

   private static Station mkStop(JSONObject station) {
      long id = Long.parseLong((String) station.get("id"));
      String name = (String) station.get("name");
      GeoLoc geoLoc = mkGeoLoc(station);
      return new Station(id, name, geoLoc);
   }

   private static GeoLoc mkGeoLoc(JSONObject station) {
      JSONObject coord = (JSONObject) station.get("coordinate");
      Double lat = (Double) coord.get("x");
      Double lng = (Double) coord.get("y");
      return new GeoLoc(lat, lng);
   }

}
