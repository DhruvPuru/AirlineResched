import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by dhruvpurushottam on 4/25/16.
 */
public class DataGenerator {

    public static HashMap<String, Integer> airportIds;
    public static String[] airports = {"ORD","JFK","LAX","MIA","ATL","IAH"};
    public static ArrayList<String> flight_paths;
    public static ArrayList<String> flight_times;
    public static double[][] airbornTimes;
    public static Random random;

    public static int NUM_AIRPORTS = airports.length;
    public static double TAXI_TIME = 0.5;

    public static void main (String[] args) {
        setup();
        generateData();
    }

    public static void generateData() {
        for (int i = 0; i < NUM_AIRPORTS; i++) {
            for (int j = 0; j < NUM_AIRPORTS; j++) {
                for (int k = 0; k < NUM_AIRPORTS; k++) {
                    if (i != j && k != j) {
                        String flight = airports[i] + "-" + airports[j] + "-" + airports[k];
                        double a_leave = roundTo2Dps(generateRandomDeparture());
                        double b_arrive = roundTo2Dps(a_leave + airbornTimes[i][j] / 60.0);
                        double b_leave = roundTo2Dps(b_arrive + TAXI_TIME + Math.min(0, gaussianTaxiOverhead()));
                        double c_arrive = roundTo2Dps(b_leave + airbornTimes[j][k] / 60.0);
                        String time = a_leave + "-" + b_arrive + "-" + b_leave + "-" + c_arrive;
                        flight_paths.add(flight);
                        flight_times.add(time);
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

    public static double generateRandomDeparture() {
        return 24 * Math.random();
    }

    private static void setup() {
        airportIds = new HashMap<String, Integer>();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        System.out.println(Arrays.deepToString(airbornTimes));
    }

}
