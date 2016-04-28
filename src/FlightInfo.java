import java.util.StringTokenizer;

/**
 * Created by dhruvpurushottam on 4/26/16.
 */
public class FlightInfo implements Comparable<FlightInfo> {

    private static int nextId = 0;

    int id;
    String fromTo;
    double ticketedDeparture;
    double ticketedArrival;
    double realDeparture;
    double realArrival;
    int capacity;
    int load;
    int source;
    int destination;
    boolean delayAdded;

    public FlightInfo(String fromTo, double ticketedDeparture, double ticketedArrival, int capacity) {
        this.fromTo = fromTo;
        this.id = nextId++;
        this.ticketedDeparture = ticketedDeparture;
        this.ticketedArrival = ticketedArrival;
        this.realDeparture = ticketedDeparture;
        this.realArrival = ticketedArrival;
        this.capacity = capacity;
        this.load = 0;
        StringTokenizer stk = new StringTokenizer(fromTo, ":");
        this.delayAdded = false;
        this.source = DataGenerator.airportIds.get(stk.nextToken());
        this.destination = DataGenerator.airportIds.get(stk.nextToken());
    }

    public void addDelays() {
        if (!delayAdded) {
            realArrival += DataGenerator.roundTo2Dps(DataGenerator.delayTimes[source][destination]);
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
        return id + ":" + fromTo + ":\n" + load + "/" + capacity + "\n"
                + "Ticketed: " + ticketedDeparture + ":" + ticketedArrival + "\n" +
                "Real: " + realDeparture + ":" + realArrival + "\n";
    }

    //Earlier departure is 'greater'
    public int compareTo(FlightInfo other) {
        if (this.realDeparture > other.realDeparture) {
            return 1;
        }
        else if (this.realDeparture < other.realDeparture) {
            return -1;
        }
        return 0;
    }
}
