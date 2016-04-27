/**
 * Created by dhruvpurushottam on 4/27/16.
 */
public class Itinerary {

    public Itinerary(FlightInfo flightInfo1, FlightInfo flightInfo2) {
        this.flightInfo1 = flightInfo1;
        this.flightInfo2 = flightInfo2;
    }

    FlightInfo flightInfo1;
    FlightInfo flightInfo2;

    public String toString() {
        return flightInfo1.toString() + "--->" + flightInfo2.toString() + "----------\n";
    }
}
