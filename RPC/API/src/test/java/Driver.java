/**
 * 
 */

/**
 * @author blake
 *
 */
public class Driver {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerInterface resSys = new ServerInterface();
		
		// Try to get a list of airports
		String xmlAirport = resSys.getAirports("WickedSmaht");
		System.out.println(xmlAirport);

		// Get a sample list of flights from server
		String xmlFlights = resSys.getFlights("WickedSmaht", "BOS", "2017_06_20");
		System.out.println(xmlFlights);
		
		// Create the aggregate flights
		Flights flights = new Flights();
		flights.addAll(xmlFlights);
		
		//try to reserve a coach seat on one of the flights
		Flight flight = flights.get(0);
		String flightNumber = flight.getmNumber();
		int seatsReservedStart = flight.getmSeatsCoach();

		System.out.println(flight.getmCodeDepart());
		System.out.println(flight.getmTimeDepart());
		System.out.println(flight.getmCodeArrival());
		System.out.println(flight.getmTimeArrival());
		System.out.println(flight.getmFlightTime());
		System.out.println(flight.getmNumber());

		
		String xmlReservation = "<Flights>"
				+ "<Flight number=\"" + flightNumber + "\" seating=\"Coach\"/>"
				+ "</Flights>";
		
		
		// Try to lock the database, purchase ticket and unlock database
		resSys.lock("WorldPlaneInc");
		resSys.buyTickets("WorldPlaneInc", xmlReservation);
		resSys.unlock("WorldPlaneInc");
		
		// Verify the operation worked
		xmlFlights = resSys.getFlights("WickedSmaht", "BOS", "2017_05_10");
		System.out.println(xmlFlights);
		flights.clear();
		flights.addAll(xmlFlights);
		
		// Find the flight number just updated
		int seatsReservedEnd = seatsReservedStart;
		for (Flight f : flights) {
			String tmpFlightNumber = f.getmNumber();
			if (tmpFlightNumber.equals(flightNumber)) {
				seatsReservedEnd = f.getmSeatsCoach();
				break;
			}
		}
		if (seatsReservedEnd == (seatsReservedStart + 1)) {
			System.out.println("Seat Reserved Successfully");
		} else {
			System.out.println("Reservation Failed");
		}
	}
}
