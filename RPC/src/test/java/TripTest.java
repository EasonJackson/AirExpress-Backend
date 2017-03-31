

import static org.junit.Assert.assertEquals;

public class TripTest {

    @org.junit.Test
    public void tripTest() {
        Trip myTrip = new Trip();
        myTrip.setTripID(1);
        assertEquals(2, myTrip.getTripID());
    }
}
