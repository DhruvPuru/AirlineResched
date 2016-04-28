import java.util.*;

/**
 * Created by dhruvpurushottam on 4/27/16.
 */
public class Rescheduler {

    public static int MAX_DELAY = 15;

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

        //Iterate over all flights, examine pre-flights and passengers on it.
        for (Map.Entry entry : flightData.entrySet()) {
            List<FlightInfo> flights = flightData.get(entry);
            // For each flight
            for (FlightInfo currentFlight : flights) {
                HashMap<FlightInfo, Integer> preFlights = currentFlight.preFlights;
                // For each preflight
                for (Map.Entry<FlightInfo, Integer> e : preFlights.entrySet()) {
                    FlightInfo f = e.getKey();
                    int numPassengers = e.getValue();
                    double simplePTD = 0;
                    double smrgolPTD = 0;
                    if (f.realArrival > DataGenerator.TRANSIT_TIME + currentFlight.realDeparture) {
                        // Find other flights for passengers, calculate PTD
                        for (int i = 0; i < numPassengers; i++) {
                            double thisTripDelay = MAX_DELAY;
                            String fromTo = currentFlight.fromTo;
                            List<FlightInfo> alternativeFlight2 = flightData.get(fromTo);
                            Collections.sort(alternativeFlight2);
                            for (FlightInfo flightInfo : alternativeFlight2) {
                                if (!flightInfo.isFull()) {
                                    flightInfo.load++;
                                    currentFlight.load--;
                                    thisTripDelay = flightInfo.realArrival - currentFlight.ticketedArrival;
                                    break;
                                }
                            }
                            simplePTD += thisTripDelay;
                        }
                    }
                    // Consider delaying current flight
                }
            }
        }
    }
}
