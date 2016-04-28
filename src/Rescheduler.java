import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dhruvpurushottam on 4/27/16.
 */
public class Rescheduler {

    public static void main(String[] args) {
        // Simple algorithm
        DataGenerator dataGenerator = new DataGenerator();
        int missed = dataGenerator.printMissedConnections();
        HashMap<String, List<FlightInfo>> flightData = dataGenerator.flightData;
        ArrayList<Itinerary> passengerItineraries = dataGenerator.passengerItineraries;

        for (Itinerary i : passengerItineraries) {
            FlightInfo f1 = i.flightInfo1;
            FlightInfo f2 = i.flightInfo2;

            // If missing connection, reschedule on earliest flight with seats
            if (f1.realArrival > f2.realDeparture + DataGenerator.TRANSIT_TIME) {
                String flight2 = f2.fromTo;
                List<FlightInfo> alternativeFlight2 = flightData.get(flight2);
                Collections.sort(alternativeFlight2);
                for (FlightInfo flightInfo : alternativeFlight2) {
//                    System.out.print("flightInfo = " + flightInfo.realDeparture);
                    if (!flightInfo.isFull()) {
                        i.flightInfo2 = flightInfo;
                        flightInfo.load++;
                        f2.load--;
                        break;
                    }
                }
//                System.out.println();
            }
        }
        int newMissed = dataGenerator.printMissedConnections();
        System.out.println("Miss ratio: " + newMissed / missed);

        //SMRGOL, assuming all constraints are soft
        dataGenerator = new DataGenerator();
        missed = dataGenerator.printMissedConnections();
        flightData = dataGenerator.flightData;
        passengerItineraries = dataGenerator.passengerItineraries;


    }
}
