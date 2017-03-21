import java.util.*;

/**
 * Created by eason on 2/26/17.
 */
public class Trips extends ArrayList<Trip> {

    public boolean append(Trip flights) {
        if(flights != null) {
            this.add(flights);
            return true;
        }
        return false;
    }

    public String toJSONText() {
        String result = "";
        Iterator<Trip> it = this.iterator();
        result += "[";
        while(it.hasNext()) {
            Trip fts = it.next();
            result += "{\"tripid\": " + fts.getTripID() + ","
                        + fts.toJSONText() + "}";
            if(it.hasNext()) {
                result += ",";
            }
        }
        result += "]";
        return result;
    }
}
