import java.util.Iterator;

/**
 * Created by eason on 3/1/17.
 */
public class Trip extends Flights{
    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
    }

    private int tripID;

    public int compareTo(Object o) {
        return 0;
    }

    public String toJSONText() {
        String result = null;
        Iterator<Flight> it = this.iterator();
        while(it.hasNext()) {
            Flight fts = it.next();
        }
        return result;
    }
}
