import java.util.*;

/**
 * Created by dhruvpurushottam on 4/27/16.
 */
public class Rescheduler {

    public static int MAX_DELAY = 15;
    public static final String SIMPLE_RESCHED = "SIMPLE RESCHEDULE";
    public static final String SRMGOL_RESCHED = "SRMGOL";
    public static final String BEFORE_RESCHED = "BEFORE RESCHEDULE";

    public static void append(String[] output) {
        String result = "";
        for (int i = 0; i < output.length; i++) {
            result += output[i];
            if (i == output.length - 1) {
                result += '\n';
            }
            else {
                result += ", ";
            }
        }
        System.out.println(result);
    }

    public static void main(String[] args) {
            int missedBeforeReschedule = 0, missedSimple = 0, missedSRMGOL = 0;
            double simplePTDTotal = 0.0, srmgolPTDTotal = 0.0, simplePlaneDelay = 0.0, srmgolPlaneDelay = 0.0;
            DataGenerator dataGenerator;
            int delayed = 0;
            HashMap<String, List<FlightInfo>> flightData;
            ArrayList<Itinerary> passengerItineraries;

            // Simple algorithm
            dataGenerator = new DataGenerator();
            missedBeforeReschedule = dataGenerator.printMissedConnections();
            HashMap<String, List<FlightInfo>> originalFlightData = dataGenerator.flightData;
            passengerItineraries = dataGenerator.passengerItineraries;
            dataGenerator.printAverageLoad();

            for (Itinerary i : passengerItineraries) {
                FlightInfo f1 = i.flightInfo1;
                FlightInfo f2 = i.flightInfo2;

                // If missing connection, reschedule on earliest flight with seats
                if (f1.realArrival + DataGenerator.TRANSIT_TIME> f2.realDeparture) {
                    String flight2 = f2.fromTo;
                    List<FlightInfo> alternativeFlight2 = originalFlightData.get(flight2);
                    Collections.sort(alternativeFlight2);
                    for (FlightInfo flightInfo : alternativeFlight2) {
                        if (!flightInfo.isFull() && flightInfo.realDeparture > f1.realArrival + DataGenerator.TRANSIT_TIME) {
                            i.flightInfo2 = flightInfo;
                            flightInfo.load++;
                            f2.load--;
                            break;
                        }
                    }
                }
            }
            // Calculate PTD
            for (Itinerary i : passengerItineraries) {
                FlightInfo f1 = i.flightInfo1;
                FlightInfo f2 = i.flightInfo2;
                if (f1.realArrival + DataGenerator.TRANSIT_TIME <= f2.realDeparture) {
                    simplePTDTotal += f2.realArrival - f2.ticketedArrival;
                }
                else {
                    simplePTDTotal += MAX_DELAY;
                }
            }

            // Total plane delay
            for (Map.Entry<String, List<FlightInfo>> entry : originalFlightData.entrySet()) {
                for (FlightInfo flightInfo : entry.getValue()) {
                    simplePlaneDelay += flightInfo.realArrival - flightInfo.ticketedArrival;
                }
            }
            missedSimple = dataGenerator.printMissedConnections();

            //Need to copy data to use same data set twice
            //SMRGOL, assuming all constraints are soft
            flightData = new HashMap<String, List<FlightInfo>>();
            for(Map.Entry<String, List<FlightInfo>> entry : originalFlightData.entrySet()) {
                flightData.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            ArrayList<Itinerary> itinerariesCopy = new ArrayList<Itinerary>();
            for (int i = 0; i < passengerItineraries.size(); i++) {
                Itinerary itinerary = passengerItineraries.get(i);
                FlightInfo f1 = itinerary.flightInfo1;
                FlightInfo f2 = itinerary.flightInfo2;
                FlightInfo f1Copy = null;
                FlightInfo f2Copy = null;

                ArrayList<FlightInfo> candidates1 = (ArrayList<FlightInfo>) flightData.get(f1.fromTo);
                for (FlightInfo flightInfo : candidates1) {
                    if (flightInfo.id == f1.id) {
                        f1Copy = flightInfo;
                    }
                }

                ArrayList<FlightInfo> candidates2 = (ArrayList<FlightInfo>) flightData.get(f2.fromTo);
                for (FlightInfo flightInfo : candidates2) {
                    if (flightInfo.id == f2.id) {
                        f2Copy = flightInfo;
                    }
                }

                Itinerary copy = new Itinerary(f1Copy, f2Copy);
                itinerariesCopy.add(copy);
            }

            double smrgolTotal = 0;
            double simpleTotal = 0;
            //Iterate over all flights, examine pre-flights and passengers on it.
            for (Map.Entry<String, List<FlightInfo>> entry : flightData.entrySet()) {
                List<FlightInfo> flights = entry.getValue();
                // For each flight
                for (int f = 0; f < flights.size(); f++) {
                    FlightInfo currentFlight = flights.get(f);
                    HashMap<Integer, Integer> preFlights = currentFlight.preFlightPassengers;
                    ArrayList<FlightInfo> listOfPreFlights = currentFlight.preFlights;
                    if (listOfPreFlights.size() == 0) {
                        continue;
                    }
                    ArrayList<FlightInfo> preFlightsToReschedule = new ArrayList<>();

                    Collections.sort(listOfPreFlights);
                    Collections.reverse(listOfPreFlights);
                    // If flight is on time to make connection, no need to reschedule
                    FlightInfo latestPreFlight = listOfPreFlights.get(0);
                    if (latestPreFlight.realArrival + DataGenerator.TRANSIT_TIME <= currentFlight.realDeparture) {
                        continue;
                    }

                    int numPassengersMissingConnection = preFlights.get(latestPreFlight.id);
                    preFlightsToReschedule.add(latestPreFlight);
                    // Look at latest preflights in decreasing order of arrival time, starting with second latest
                    for (int i = 1; i < listOfPreFlights.size(); i++) {
                        FlightInfo currentPreFlight = listOfPreFlights.get(i);
                        if (currentPreFlight.realArrival + DataGenerator.TRANSIT_TIME <= currentFlight.realDeparture) {
                            break;
                        }
                        preFlightsToReschedule.add(currentPreFlight);
                        numPassengersMissingConnection += preFlights.get(currentPreFlight.id);
                    }

                    // Option 1, reschedule passengers missing currentFlight to another flight
                    double simplePTD = 0;
                    for (FlightInfo preFlightToReschedule : preFlightsToReschedule) {
                        int newRemPassengers = preFlights.get(preFlightToReschedule.id);

                        // While we manage to reschedule more passengers, keep going
                        String fromTo = currentFlight.fromTo;
                        List<FlightInfo> alternativeFlight2 = flightData.get(fromTo);
                        Collections.sort(alternativeFlight2);
                        for (int i = 0; newRemPassengers > 0 && i < alternativeFlight2.size(); i++) {
                            FlightInfo flightInfo = alternativeFlight2.get(i);
                            if (flightInfo.realDeparture > preFlightToReschedule.realArrival + DataGenerator.TRANSIT_TIME
                                    && !flightInfo.isFull()) {
                                int spaceLeft = flightInfo.spaceLeft();
                                int reAssigned = Math.min(newRemPassengers, spaceLeft);
                                newRemPassengers -= reAssigned;
                                simplePTD += reAssigned * (flightInfo.realArrival - currentFlight.ticketedArrival);
                            }
                        }
                        //unassigned passengers
                        simplePTD += newRemPassengers * MAX_DELAY;
                    }
                    simplePTD += (currentFlight.load - numPassengersMissingConnection) *
                            (currentFlight.realArrival - currentFlight.ticketedArrival);

                    // Option 2, wait for latest preFlight
                    double newDeparture = latestPreFlight.realArrival + DataGenerator.TRANSIT_TIME;
                    double newArrival = currentFlight.realArrival + (newDeparture - currentFlight.realDeparture);
                    double smrgolPTD = currentFlight.load * (newArrival - currentFlight.ticketedArrival);

                    smrgolTotal += Math.min(smrgolPTD, simplePTD);
                    simpleTotal += simplePTD;

                    //If smrgol is better, make plane wait
                    if (smrgolPTD < simplePTD) {
                        currentFlight.realDeparture = newDeparture;
                        currentFlight.realArrival = newArrival;
                        delayed++;
                    }
//                System.out.println("smrgolPTD = " + smrgolPTD);
//                System.out.println("simplePTD = " + simplePTD);
                }
            }

            // Make changes again
            for (Itinerary i : itinerariesCopy) {
                FlightInfo f1 = i.flightInfo1;
                FlightInfo f2 = i.flightInfo2;

                // If missing connection, reschedule on earliest flight with seats
                if (f1.realArrival + DataGenerator.TRANSIT_TIME> f2.realDeparture) {
                    String flight2 = f2.fromTo;
                    List<FlightInfo> alternativeFlight2 = flightData.get(flight2);
                    Collections.sort(alternativeFlight2);
                    for (FlightInfo flightInfo : alternativeFlight2) {
                        if (!flightInfo.isFull() &&
                                flightInfo.realDeparture > f1.realArrival + DataGenerator.TRANSIT_TIME) {
                            i.flightInfo2 = flightInfo;
                            flightInfo.load++;
                            f2.load--;
                            break;
                        }
                    }
                }
            }

            // Calculate PTD
            for (Itinerary i : itinerariesCopy) {
                FlightInfo f1 = i.flightInfo1;
                FlightInfo f2 = i.flightInfo2;
                if (f1.realArrival + DataGenerator.TRANSIT_TIME <= f2.realDeparture) {
                    srmgolPTDTotal += f2.realArrival - f2.ticketedArrival;
                }
                else {
                    srmgolPTDTotal += MAX_DELAY;
                }
            }

            for (Map.Entry<String, List<FlightInfo>> entry : flightData.entrySet()) {
                for (FlightInfo flightInfo : entry.getValue()) {
                    srmgolPlaneDelay += flightInfo.realArrival - flightInfo.ticketedArrival;
                }
            }

            missedSRMGOL = dataGenerator.printMissedConnections();
            System.out.println("Simple: " + simplePTDTotal);
            System.out.println("SMRGOL: " + srmgolPTDTotal);
            System.out.println("missedBeforeReschedule = " + missedBeforeReschedule);
            System.out.println("missedSimple = " + missedSimple);
            System.out.println("missedSRMGOL = " + missedSRMGOL);
            System.out.println("simplePlaneDelay = " + simplePlaneDelay);
            System.out.println("srmgolPlaneDelay = " + srmgolPlaneDelay);
            String[] output = {"" + simplePTDTotal, "" + srmgolPTDTotal, "" + missedBeforeReschedule, "" + missedSimple,
            "" + missedSRMGOL, "" + simplePlaneDelay, "" + srmgolPlaneDelay};
            append(output);
    }
}
