import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by eason on 4/4/17.
 */
public class Operation_reserveTrip {
    @Test
    public void reserverTripTest() {
        ServerInterface sys = new ServerInterface();
        String TEAM_DB = "WickedSmaht";
        String Airport_Code = "BOS";
        String Date = "2017_05_12";

        HashMap<String, Airplane> AIRPLANES = new HashMap<String, Airplane>();
        Airplanes airplanes = new Airplanes();
        airplanes.addAll(sys.getAirplanes(TEAM_DB));
        for(Airplane airplane : airplanes) {
            AIRPLANES.put(airplane.model(), airplane);
        }

        Flights flights = new Flights();
        flights.addAll(sys.getFlightsDeparting(TEAM_DB, Airport_Code, Date));

        Flight flight_0 = flights.get(0);
        assertNotNull(flight_0);
        System.out.println("Depart: " + flight_0.getmCodeDepart() + " "
                + "Arrival: " + flight_0.getmCodeArrival() + " "
                + "Flight #: " + flight_0.getmNumber() + " "
                + "Seat Coach: " + flight_0.getmSeatsCoach());

        Flight flight_1 = flights.get(1);
        assertNotNull(flight_1);
        System.out.println("Depart: " + flight_1.getmCodeDepart() + " "
                + "Arrival: " + flight_1.getmCodeArrival() + " "
                + "Flight #: " + flight_1.getmNumber() + " "
                + "Seat Coach: " + flight_1.getmSeatsCoach());

        sys.lock(TEAM_DB);
        String typeOfSeat = "Coach";
        for(int i = 0; i < 2; i++) {
            String xmlReservation = "<Flights>"
                    + "<Flight number=\"" + flights.get(i).getmNumber() + "\" seating=\""
                    + typeOfSeat + "\"/>" + "</Flights>";
            if(flights.get(i).getmSeatsCoach() >= AIRPLANES.get(flights.get(i).getmAirplane()).coachSeats()) {
                System.out.println("No seat available. Reservation failed.");
                sys.unlock(TEAM_DB);
                return;
            }
            sys.buyTickets(TEAM_DB, xmlReservation);
        }

        sys.unlock(TEAM_DB);

        Flights flights_check = new Flights();
        flights_check.addAll(sys.getFlightsDeparting(TEAM_DB, Airport_Code, Date));
        Flight flight_00 = flights_check.get(0);
        assertNotNull(flight_00);
        assertEquals(flight_0.getmSeatsCoach() + 1, flight_00.getmSeatsCoach());
        System.out.println("Depart: " + flight_00.getmCodeDepart()
                + "Arrival: " + flight_00.getmCodeArrival()
                + "Flight #: " + flight_00.getmNumber()
                + "Seat Coach: " + flight_00.getmSeatsCoach());

        Flight flight_01 = flights_check.get(1);
        assertNotNull(flight_01);
        assertEquals(flight_1.getmSeatsCoach() + 1, flight_01.getmSeatsCoach());
        System.out.println("Depart: " + flight_01.getmCodeDepart() + " "
                + "Arrival: " + flight_01.getmCodeArrival() + " "
                + "Flight #: " + flight_01.getmNumber() + " "
                + "Seat Coach: " + flight_01.getmSeatsCoach());
    }
}
