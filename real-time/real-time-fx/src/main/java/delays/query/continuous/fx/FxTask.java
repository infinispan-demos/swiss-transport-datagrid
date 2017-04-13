package delays.query.continuous.fx;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class FxTask extends Task<Void> {

   private ObservableList<StationBoardView> partialResults =
         FXCollections.observableArrayList();

//   private Future<Void> injectorFuture;
//   private ContinuousQuery<Station, StationBoard> continuousQuery;
//   private RemoteCache<Station, StationBoard> stationBoards;
//   private RemoteCacheManager client;

   private BlockingQueue<StationBoardView> queue = new ArrayBlockingQueue<>(128);

   public final ObservableList<StationBoardView> getPartialResults() {
      return partialResults;
   }

   @Override
   protected Void call() throws Exception {
      while (true) {
         if (isCancelled()) break;
         StationBoardView entry = queue.poll(1, TimeUnit.SECONDS);
         Thread.sleep(200);
         if (entry != null) {
            Platform.runLater(() ->
                  partialResults.add(entry));
         }
      }
      return null;
   }

}
