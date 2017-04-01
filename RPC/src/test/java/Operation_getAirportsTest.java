import org.junit.Assert;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
/**
 * Created by eason on 4/1/17.
 */
public class Operation_getAirportsTest {
    @org.junit.Test
    public void getAirportsTest() {
        Airports AIRPORTS;
        HashSet<String> AIRPORTS_CODE = new HashSet<String>();
        ServerInterface sys = new ServerInterface();
        String TEAM_DB = "WickedSmaht";
            AIRPORTS = new Airports();
            AIRPORTS.addAll(sys.getAirports(TEAM_DB));
            for(Airport airport : AIRPORTS) {
                AIRPORTS_CODE.add(airport.code());
            }
        Assert.assertNotNull(AIRPORTS);
        for(Airport air : AIRPORTS) {
            Assert.assertTrue(air.code().matches("..."));
            System.out.println(air.name() + " " + air.code() + " " + air.latitude() + " " + air.longitude());
        }
    }
}
