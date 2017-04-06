

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TripTest {

    @Test
    public void tripTest() {
        Trip myTrip = new Trip();
        myTrip.setTripID(1);
        assertEquals(2, myTrip.getTripID());
    }
}
