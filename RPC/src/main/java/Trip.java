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
        result =  "\"value\"" + ": " + "[";
        Iterator<Flight> it = this.iterator();
        int count = 0;
        while(it.hasNext()) {
            Flight fts = it.next();
            result += "{\"leg\": {";
            result += "\"id\": "                + "\""  + Integer.toString(count)   + "\"" + ",";
            result += "\"Airplane\": "          + "\""  + fts.getmAirplane()        + "\"" + ",";
            result += "\"FlightTime\": "        + "\""  + fts.getmFlightTime()      + "\"" + ",";
            result += "\"Number\": "            + "\""  + fts.getmNumber()          + "\"" + ",";
            result += "\"CodeDepart\": "        + "\""  + fts.getmCodeDepart()      + "\"" + ",";
            result += "\"TimeDepart\": "        + "\""  + fts.getmTimeDepart()      + "\"" + ",";
            result += "\"CodeArrival\": "       + "\""  + fts.getmCodeArrival()     + "\"" + ",";
            result += "\"TimeArrival\": "       + "\""  + fts.getmTimeArrival()     + "\"" + ",";
            result += "\"PriceFirstclass\": "   + "\""  + fts.getmPriceFirstclass() + "\"" + ",";
            result += "\"SeatsFirstclass\": "   + "\""  + fts.getmSeatsFirstclass() + "\"" + ",";
            result += "\"PriceCoach\": "        + "\""  + fts.getmPriceCoach()      + "\"" + ",";
            result += "\"SeatsCoach\": "        + "\""  + fts.getmSeatsCoach()      + "\"" + "}";
            result += "}";
            if(it.hasNext()) {
                result += ",";
            }
            count ++;
        }
        return result;
    }
}
