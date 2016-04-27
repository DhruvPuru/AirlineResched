import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by dhruvpurushottam on 4/25/16.
 */
public class DataGenerator {

    public static HashMap<String, Integer> airportIds;
    //Key = airport-airport (from-to). Value = {departure, capacity etc...}
    public static HashMap<String, List<FlightInfo>> flightData;
    public static ArrayList<Itinerary> passengerItineraries;
    public static String[] airports = {"ORD","JFK","LAX","MIA","ATL","IAH"};
    public static double[][] airbornTimes;
    public static double[][] delayTimes;
    public static int[][] flightCapacities;
    public static Random random;
    public static int totalPassengers;
    public static int passengerId;

    public static int NUM_AIRPORTS = airports.length;
    public static double TAXI_TIME = 0.5;
    public static double TRANSIT_TIME = 0.5;

    public static void main (String[] args) {
        setup();
        generateData();
//        printData();
//        printItinerary();
        printMissedConnections();
        printAverageLoad();
    }

    public static void generateData() {
        for (int i = 0; i < NUM_AIRPORTS; i++) {
            for (int j = 0; j < NUM_AIRPORTS; j++) {
                for (int k = 0; k < NUM_AIRPORTS; k++) {
                    if (i != j && k != j) {
                        //Create schedule
                        double a_leave = roundTo2Dps(Math.max(generateNormalizedDeparture(), 0));
                        double b_arrive = roundTo2Dps(a_leave + airbornTimes[i][j]);
                        double b_leave = roundTo2Dps(b_arrive + TAXI_TIME + Math.max(0, gaussianTaxiOverhead()));
                        //TODO: Change to reflect delays
                        double c_arrive = roundTo2Dps(b_leave + (airbornTimes[j][k]));

                        //Create mapping of flights
                        //TODO: make flight capacities reflect some distribution + hard/soft constraints
                        String flight1 = airports[i] + ":" + airports[j];
                        FlightInfo flightInfo1 = new FlightInfo(flight1, a_leave, b_arrive, flightCapacities[i][j]);
                        String flight2 = airports[j] + ":" + airports[k];
                        FlightInfo flightInfo2 = new FlightInfo(flight2, b_leave, c_arrive, flightCapacities[i][j]);

                        List<FlightInfo> sameJourneyAsFlight1 = flightData.get(flight1);
                        if (sameJourneyAsFlight1 == null) {
                            sameJourneyAsFlight1 = new ArrayList<FlightInfo>();
                            flightData.put(flight1, sameJourneyAsFlight1);
                        }
                        sameJourneyAsFlight1.add(flightInfo1);

                        List<FlightInfo> sameJourneyAsFlight2 = flightData.get(flight2);
                        if (sameJourneyAsFlight2 == null) {
                            sameJourneyAsFlight2 = new ArrayList<FlightInfo>();
                            flightData.put(flight2, sameJourneyAsFlight2);
                        }
                        sameJourneyAsFlight2.add(flightInfo2);
                        //Update total passengers in model
                        totalPassengers += Math.min(flightCapacities[i][j], flightCapacities[j][k]);
                    }
                }
            }
        }

        //Map passenger to flight path from airport A to B to C s.t A != B and B!= C
        //TODO:remove assumption that all passengers have connecting flights
        for (int p = 0; p < totalPassengers; p++) {
            boolean matched = false;
            while (!matched) {
                //Select A, B and C at random
                int a = randomIndex(6);
                int b = a;
                while (a == b) {
                    b = randomIndex(6);
                }
                int c = b;
                while (c == b) {
                    c = randomIndex(6);
                }

                String flight1 = airports[a] + ":" + airports[b];
                String flight2 = airports[b] + ":" + airports[c];
                List<FlightInfo> candidatesForFlight1 = flightData.get(flight1);
                List<FlightInfo> candidatesForFlight2 = flightData.get(flight2);

                int[] sequence1 = generateRandomPermutation(candidatesForFlight1.size());
                int[] sequence2 = generateRandomPermutation(candidatesForFlight2.size());
                for (int i = 0; i < sequence1.length && !matched; i++) {
                    for (int j = 0; j < sequence2.length && !matched; j++) {
                        FlightInfo flight1Candidate = candidatesForFlight1.get(sequence1[i]);
                        FlightInfo flight2Candidate = candidatesForFlight2.get(sequence2[j]);
                        if (flight1Candidate.arrival <= TRANSIT_TIME + flight2Candidate.departure &&
                                !flight1Candidate.isFull() && !flight2Candidate.isFull()) {
                            passengerItineraries.add(new Itinerary(flight1Candidate, flight2Candidate));
                            flight1Candidate.load++;
                            flight2Candidate.load++;
                            matched = true;
                        }
                    }
                }
            }
            passengerId++;
        }

        //Add delays
        for (Map.Entry entry : flightData.entrySet()) {
            List<FlightInfo> flightInfos = (List<FlightInfo>) entry.getValue();
            for (FlightInfo flightInfo : flightInfos) {
                flightInfo.addDelays();
            }
        }
    }

    public static void printAverageLoad() {
        double sumRatio = 0;
        int totalFlights = 0;
        for (Map.Entry entry : flightData.entrySet()) {
            List<FlightInfo> flightInfos = (List<FlightInfo>) entry.getValue();
            for (FlightInfo flightInfo : flightInfos) {
                sumRatio += flightInfo.load / flightInfo.capacity;
                totalFlights++;
            }
        }
        System.out.println("Average load ratio: " + sumRatio/totalFlights);
    }

    public static void printMissedConnections() {
        int numberMissed = 0;
        for (Itinerary i : passengerItineraries) {
            if (i.flightInfo1.arrival > i.flightInfo2.departure) {
                System.out.println("Missed connection:\n" + i);
                numberMissed++;
            }
        }
        System.out.println("NUMBER MISSED: " + numberMissed);
    }

    public static void printItinerary() {
        for (Itinerary i : passengerItineraries) {
            System.out.println(i);
        }
    }

    public static int[] generateRandomPermutation(int n) {
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            int d = random.nextInt(i+1);
            result[i] = result[d];
            result[d] = i;
        }
        return result;
    }


    public static int randomIndex(double max) {
        double offset = 0.000001;
        return (int) Math.floor((max-offset) * random.nextDouble());
    }

    public static double roundTo2Dps(double d) {
        return Math.round(d * 100) / 100.0;
    }

    public static double gaussianTaxiOverhead() {
        return random.nextGaussian();
    }

    public static double generateNormalizedDeparture() {
        return 12 + (6 * random.nextGaussian());
    }

    public static double generateRandomDeparture() {
        return 24 * Math.random();
    }

    public static void printData() {
        for (Map.Entry entry:flightData.entrySet()) {
            System.out.println(entry.getValue());
        }
    }

    private static void setup() {
        airportIds = new HashMap<>();
        flightData = new HashMap<>();
        passengerItineraries = new ArrayList<>();

        random = new Random();
        for (int i = 0; i < NUM_AIRPORTS; i++) {
            airportIds.put(airports[i], i);
        }

        try {
            File airbornTimeFile = new File("airbornTime.txt");
            Scanner in = new Scanner(airbornTimeFile);
            airbornTimes = new double[NUM_AIRPORTS][NUM_AIRPORTS];
            for (int i = 0; i < NUM_AIRPORTS; i++) {
                for (int j = 0; j < NUM_AIRPORTS; j++) {
                    double timeFromIToJ = in.nextDouble() / 60.0;
                    airbornTimes[i][j] = timeFromIToJ;
                }
            }
            in.close();

            File delayTimeFile = new File("Delay-Times.txt");
            in = new Scanner(delayTimeFile);
            delayTimes = new double[NUM_AIRPORTS][NUM_AIRPORTS];
            for (int i = 0; i < NUM_AIRPORTS; i++) {
                for (int j = 0; j < NUM_AIRPORTS; j++) {
                    double timeFromIToJ = in.nextDouble() / 60.0;
                    delayTimes[i][j] = timeFromIToJ;
                }
            }
            in.close();

            File flightCapacitiesFile = new File("flight-capacities.txt");
            in = new Scanner(flightCapacitiesFile);
            flightCapacities = new int[NUM_AIRPORTS][NUM_AIRPORTS];
            for (int i = 0; i < NUM_AIRPORTS; i++) {
                for (int j = 0; j < NUM_AIRPORTS; j++) {
                    int capacityIToJ = in.nextInt();
                    flightCapacities[i][j] = capacityIToJ;
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
