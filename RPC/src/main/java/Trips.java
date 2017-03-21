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

    private class PriceComparator implements Comparator<Trip> {
        public int compare(Trip f1, Trip f2) {
            double price1 = 0.0;
            double price2 = 0.0;
            for(Flight f : f1) {
                price1 += Integer.getInteger(f.getmPriceCoach().substring(1).trim());
            }
            for(Flight f: f2) {
                price2 += Integer.getInteger(f.getmPriceCoach().substring(1).trim());
            }

            if(price1 > price2) {
                return 1;
            } else if(price1 < price2) {
                return -1;
            }
            return 0;
        }
    }

    private class DurationComparator implements Comparator<Trip> {
        public int compare(Trip f1, Trip f2) {
            int duration1 = 0;
            int duration2 = 0;
            for(Flight f : f1) {
                duration1 += Integer.getInteger(f.getmFlightTime());
            }

            for(Flight f : f2) {
                duration2 += Integer.getInteger(f.getmFlightTime());
            }

            return duration1 - duration2;
        }
    }
}
