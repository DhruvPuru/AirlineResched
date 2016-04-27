import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by dhruvpurushottam on 4/25/16.
 */
public class DataGenerator {

    public static HashMap<String, Integer> airportIds;
    //Key = airport-airport (from-to). Value = {departure, capacity etc...}
    public static HashMap<String, List<FlightInfo>> flightsToInfo;
    public static String[] airports = {"ORD","JFK","LAX","MIA","ATL","IAH"};
    public static ArrayList<String> flight_paths;
    public static ArrayList<String> flight_times;
    public static double[][] airbornTimes;
    public static double[][] delayTimes;
    public static Random random;

    public static int NUM_AIRPORTS = airports.length;
    public static double TAXI_TIME = 0.5;
    public static int DEFAULT_CAPACITY = 100;

    public static void main (String[] args) {
        setup();
        generateData();
    }

    public static void generateData() {
        for (int i = 0; i < NUM_AIRPORTS; i++) {
            for (int j = 0; j < NUM_AIRPORTS; j++) {
                for (int k = 0; k < NUM_AIRPORTS; k++) {
                    if (i != j && k != j) {
                        //Create schedule
                        String flight = airports[i] + "-" + airports[j] + "-" + airports[k];
                        double a_leave = roundTo2Dps(Math.max(generateNormalizedDeparture(), 0));
                        double b_arrive = roundTo2Dps(a_leave + airbornTimes[i][j]/ 60.0);
                        double b_leave = roundTo2Dps(b_arrive + TAXI_TIME + Math.max(0, gaussianTaxiOverhead()));
                        //Change b_arrive to reflect delays
                        b_arrive = roundTo2Dps(b_arrive + delayTimes[i][j]/ 60.0);
                        double c_arrive = roundTo2Dps(b_leave + (airbornTimes[j][k] + delayTimes[j][k])/ 60.0);

                        String time = a_leave + "-" + b_arrive + "-" + b_leave + "-" + c_arrive;
                        flight_paths.add(flight);
                        flight_times.add(time);

                        //Create mapping of flights
                        //TODO: make flight capacities reflect some distribution + hard/soft constraints
                        String flight1 = airports[i] + ":" + airports[j];
                        FlightInfo f1 = new FlightInfo(a_leave, b_arrive, DEFAULT_CAPACITY);
                        String time2 = b_leave + "-" + c_arrive;
                        FlightInfo f2 = new FlightInfo(b_leave, c_arrive, DEFAULT_CAPACITY);

//                        List<FlightInfo> flight1Candidates =
                    }
                }
            }
        }

        for (int i = 0; i < flight_paths.size(); i++) {
            System.out.println(flight_paths.get(i) + "::::" + flight_times.get(i));
        }
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

    private static void setup() {
        airportIds = new HashMap<String, Integer>();
        flightsToInfo = new HashMap<String, List<FlightInfo>>();
        flight_paths = new ArrayList<String>();
        flight_times = new ArrayList<String>();

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
                    double timeFromIToJ = in.nextDouble();
                    airbornTimes[i][j] = timeFromIToJ;
                }
            }
            in.close();

            File delayTimeFile = new File("Delay-Times.txt");
            in = new Scanner(delayTimeFile);
            delayTimes = new double[NUM_AIRPORTS][NUM_AIRPORTS];
            for (int i = 0; i < NUM_AIRPORTS; i++) {
                for (int j = 0; j < NUM_AIRPORTS; j++) {
                    double timeFromIToJ = in.nextDouble();
                    System.out.print(timeFromIToJ + "\t");
                    delayTimes[i][j] = timeFromIToJ;
                }
                System.out.println();
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        System.out.println(Arrays.deepToString(airbornTimes));
    }

}
