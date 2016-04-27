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

    public FlightInfo(String fromTo, double departure, double arrival, int capacity) {
        this.fromTo = fromTo;
        this.id = nextId++;
        this.departure = departure;
        this.arrival = arrival;
        this.capacity = capacity;
        this.load = 0;
    }

    public String toString() {
        return id + ":" + fromTo + ":" + departure + ":" + arrival + ":" + load + "/" + capacity + "\n";
    }
}
