package delays.query.continuous.fx;

import java.util.Comparator;

import javafx.beans.property.SimpleStringProperty;

public class StationBoardView {

   private final SimpleStringProperty type;
   private final SimpleStringProperty departure;
   private final SimpleStringProperty station;
   private final SimpleStringProperty destination;
   private final SimpleStringProperty delay;
   private final SimpleStringProperty trainName;

   public StationBoardView(String type,
         String departure,
         String station,
         String destination,
         String delay,
         String trainName) {
      this.type = new SimpleStringProperty(type);
      this.departure = new SimpleStringProperty(departure);
      this.station = new SimpleStringProperty(station);
      this.destination = new SimpleStringProperty(destination);
      this.delay = new SimpleStringProperty(delay);
      this.trainName = new SimpleStringProperty(trainName);
   }

   public String getType() {
      return type.get();
   }

   public SimpleStringProperty typeProperty() {
      return type;
   }

   public void setType(String type) {
      this.type.set(type);
   }

   public String getDeparture() {
      return departure.get();
   }

   public SimpleStringProperty departureProperty() {
      return departure;
   }

   public void setDeparture(String departure) {
      this.departure.set(departure);
   }

   public String getStation() {
      return station.get();
   }

   public SimpleStringProperty stationProperty() {
      return station;
   }

   public void setStation(String station) {
      this.station.set(station);
   }

   public String getDestination() {
      return destination.get();
   }

   public SimpleStringProperty destinationProperty() {
      return destination;
   }

   public void setDestination(String destination) {
      this.destination.set(destination);
   }

   public String getDelay() {
      return delay.get();
   }

   public SimpleStringProperty delayProperty() {
      return delay;
   }

   public void setDelay(String delay) {
      this.delay.set(delay);
   }

   public String getTrainName() {
      return trainName.get();
   }

   public SimpleStringProperty trainNameProperty() {
      return trainName;
   }

   public void setTrainName(String trainName) {
      this.trainName.set(trainName);
   }

   public static Comparator<StationBoardView> comparator() {
      return new DepatureComparator();
   }

   static final class DepatureComparator implements Comparator<StationBoardView> {

      @Override
      public int compare(StationBoardView o1, StationBoardView o2) {
         return o2.getDeparture().compareTo(o1.getDeparture());
      }

   }

}
