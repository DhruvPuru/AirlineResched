import java.util.HashMap;

/**
 * Created by dhruvpurushottam on 4/25/16.
 */
public class DataGenerator {

    public static HashMap<String, Integer> airportIds = new HashMap<>();
    public static String[] airports = {"ORD","JFK","LAX","MIA","ATL","IAH"};
    {
        for (int i = 0; i < airports.length; i++) {
            airportIds.put(airports[i], i);
        }
    }

    public static void main (String[] args) {
        
    }
}
