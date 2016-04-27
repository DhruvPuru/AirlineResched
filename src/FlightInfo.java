import javax.xml.crypto.Data;
import java.util.StringTokenizer;

/**
 * Created by dhruvpurushottam on 4/26/16.
 */
public class FlightInfo {

    private static int nextId = 0;

    int id;
    String fromTo;
    double departure;
    double arrival;
    int capacity;
    int load;
    int source;
    int destination;
    boolean delayAdded;

    public FlightInfo(String fromTo, double departure, double arrival, int capacity) {
        this.fromTo = fromTo;
        this.id = nextId++;
        this.departure = departure;
        this.arrival = arrival;
        this.capacity = capacity;
        this.load = 0;
        StringTokenizer stk = new StringTokenizer(fromTo, ":");
        this.delayAdded = false;
        this.source = DataGenerator.airportIds.get(stk.nextToken());
        this.destination = DataGenerator.airportIds.get(stk.nextToken());
    }

    public void addDelays() {
        if (!delayAdded) {
            arrival += DataGenerator.delayTimes[source][destination];
            delayAdded = true;
        }
        else {
            System.out.println("Trying to add delay again");
        }
    }

    public boolean isFull() {
        return load == capacity;
    }

    public String toString() {
        return id + ":" + fromTo + ":" + departure + ":" + arrival + ":" + load + "/" + capacity + "\n";
    }
}
