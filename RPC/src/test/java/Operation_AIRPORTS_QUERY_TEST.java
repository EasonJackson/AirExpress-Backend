import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.model.LatLng;
import org.junit.Test;

import java.util.Iterator;
import java.util.TimeZone;

import static org.junit.Assert.assertNotNull;

/**
 * Created by eason on 4/2/17.
 */
public class Operation_AIRPORTS_QUERY_TEST {
    String TEAM_DB = "WickedSmaht";
    @Test
    public void Operation_AIRPORTS_QUERY_TEST() {
        Airports AIRPORTS;
        String AIRPORTS_QUERY;
        ServerInterface sys = new ServerInterface();
        AIRPORTS = new Airports();
        AIRPORTS.addAll(sys.getAirports(TEAM_DB));
        GeoApiContext ctx = new GeoApiContext().setApiKey("AIzaSyAmGNL2f_7a172eqp4YPnmTU-eqQFzWcNk");
        TimeZone tz;
        LatLng location;
        String query = "[";
        Iterator<Airport> iter = AIRPORTS.iterator();
        while(iter.hasNext()) {
            Airport airport = iter.next();
            long offset = 0;
            location = new LatLng(airport.latitude(), airport.longitude());
            try {
                tz = TimeZoneApi.getTimeZone(ctx, location).await();
                assertNotNull(tz);
                offset = tz.getRawOffset() + tz.getDSTSavings();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(airport.code().equals("SJC")) {
                airport.name("Mineta San Jose International");
            }
            query += "{ \"Name\": " + "\"" + airport.name() + "\"," +
                    "\"Code\": " + "\"" + airport.code() + "\"," +
                    "\"Latitude\": " + airport.latitude() + "," +
                    "\"Longitude\": " + airport.longitude() + "," +
                    "\"Offset\": "+ offset + "}";
            if(iter.hasNext()) {
                query += ",";
            }
        }
        query += "]";
        System.out.println(query);
    }
}
