import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by eason on 2/26/17.
 */
public class Operation {
    private static ServerInterface sys = new ServerInterface();
    private static final String TEAM_DB = "WickedSmaht";
    private static Trips listOfTrips = new Trips();
    private static Trips listOfTrips_return = new Trips();
    private static Flights check = new Flights();

    public static JSONRPC2Response process(String depAIR,
                                           String arrAIR,
                                           String depTime,
                                           String retTime,
                                           Object id) {

        String query = null;
        String query_return = null;
        String flight_leg1;
        String flight_leg2;
        String flight_leg3;
        HashMap<Flight, Flights> search_two = new HashMap<Flight, Flights>();
        HashMap<Flights, Flights> search_three = new HashMap<Flights, Flights>();

        if(depAIR == null || arrAIR == null || depTime == null) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, id);
        }

        listOfTrips.clear();

        //Search the DB: the real logic part
        flight_leg1 = sys.getFlights(TEAM_DB, depAIR, depTime);
        Flights search_one = new Flights();
        search_one.addAll(flight_leg1);
        for(Flight f : search_one) {
            if(f.isValid()) {
                if (f.getmCodeArrival().equals(arrAIR)) {
                    // For direct flight it will be added straight to the result listOfTrips
                    Trip li = new Trip();
                    li.add(f);
                    listOfTrips.append(li); // Adding direct flight to the listOfTrips
                } else {
                    // Search for the second leg and store the result temporarily in the HashMap search_two
                    flight_leg2 = sys.getFlights(TEAM_DB, f.getmCodeArrival(), f.getmTimeArrival());
                    Flights search_second = new Flights();
                    search_second.addAll(flight_leg2);
                    search_two.put(f, search_second);
                }
            }
        }

        for(Flight f : search_two.keySet()) {
            Flights flights = search_two.get(f);
            for(Flight f_s : flights) {
                if(f_s.isValid()) {
                    Trip li = new Trip();
                    li.add(f);
                    li.add(f_s);
                    if (f_s.getmCodeArrival().equals(arrAIR)) {
                        // For 1 leg flight it will be added to the result listOfTrips when the second flight has the arrAIR code the same as the destination
                        listOfTrips.append(li);
                    } else {
                        flight_leg3 = sys.getFlights(TEAM_DB, f_s.getmCodeArrival(), f_s.getmTimeArrival());
                        Flights search_third = new Flights();
                        search_third.addAll(flight_leg3);
                        search_three.put(li, search_third);
                    }
                }
            }
        }

        for(Flights f : search_three.keySet()) {
            Flights flights = search_three.get(f);
            for(Flight f_t : flights) {
                if(f_t.isValid() && f_t.getmCodeArrival().equals(arrAIR)) {
                    Trip fin = new Trip();
                    fin.addAll(flights);
                    fin.add(f_t);
                    listOfTrips.append(fin);
                }
            }
        }

        query = listOfTrips.toJSONText();

        if(retTime != null) {
            query_return = round(arrAIR, depAIR, retTime);
        }

        JSONRPC2Response resp = new JSONRPC2Response(query + "\n" + query_return, id);

        return resp;
    }

    public static String round(String depAIR,
                               String arrAIR,
                               String depTime) {
        String query = null;
        String flight_leg1;
        String flight_leg2;
        String flight_leg3;
        HashMap<Flight, Flights> search_two = new HashMap<Flight, Flights>();
        HashMap<Flights, Flights> search_three = new HashMap<Flights, Flights>();

        if(depAIR == null || arrAIR == null || depTime == null) {
            return query;
        }

        listOfTrips_return.clear();

        //Search the DB: the real logic part
        flight_leg1 = sys.getFlights(TEAM_DB, depAIR, depTime);
        Flights search_one = new Flights();
        search_one.addAll(flight_leg1);
        for(Flight f : search_one) {
            if(f.isValid()) {
                if (f.getmCodeArrival().equals(arrAIR)) {
                    // For direct flight it will be added straight to the result listOfTrips
                    Trip li = new Trip();
                    li.add(f);
                    listOfTrips_return.append(li); // Adding direct flight to the listOfTrips
                } else {
                    // Search for the second leg and store the result temporarily in the HashMap search_two
                    flight_leg2 = sys.getFlights(TEAM_DB, f.getmCodeArrival(), f.getmTimeArrival());
                    Flights search_second = new Flights();
                    search_second.addAll(flight_leg2);
                    search_two.put(f, search_second);
                }
            }
        }

        for(Flight f : search_two.keySet()) {
            Flights flights = search_two.get(f);
            for(Flight f_s : flights) {
                if(f_s.isValid()) {
                    Trip li = new Trip();
                    li.add(f);
                    li.add(f_s);
                    if (f_s.getmCodeArrival().equals(arrAIR)) {
                        // For 1 leg flight it will be added to the result listOfTrips when the second flight has the arrAIR code the same as the destination
                        listOfTrips_return.append(li);
                    } else {
                        flight_leg3 = sys.getFlights(TEAM_DB, f_s.getmCodeArrival(), f_s.getmTimeArrival());
                        Flights search_third = new Flights();
                        search_third.addAll(flight_leg3);
                        search_three.put(li, search_third);
                    }
                }
            }
        }

        for(Flights f : search_three.keySet()) {
            Flights flights = search_three.get(f);
            for(Flight f_t : flights) {
                if(f_t.isValid() && f_t.getmCodeArrival().equals(arrAIR)) {
                    Trip fin = new Trip();
                    fin.addAll(flights);
                    fin.add(f_t);
                    listOfTrips_return.append(fin);
                }
            }
        }

        query = listOfTrips_return.toJSONText();
        return query;
    }

    public static JSONRPC2Response sortByPrice(Object id) {
        String query = null;
        listOfTrips.sortByPrice();
        listOfTrips_return.sortByPrice();
        query = listOfTrips.toJSONText() + "\n" + listOfTrips_return.toJSONText();//Need to design the query
        return new JSONRPC2Response(query, id);
    }

    public static JSONRPC2Response sortByDuration(Object id) {
        String query = null;
        listOfTrips.sortByDuration();
        listOfTrips_return.sortByDuration();
        query = listOfTrips.toJSONText() + "\n" + listOfTrips_return.toJSONText(); //Need to design the query
        return new JSONRPC2Response(query, id);
    }

    public static JSONRPC2Response getFlightDetails(String tripNumber,
                                                    Object id) {
        String query = null;
        Iterator<Trip> it = listOfTrips.iterator();

        while(it.hasNext()) {
            if(Integer.getInteger(tripNumber) == it.next().getTripID()) {
                query += it.next().toJSONText();
                break;
            }
        }

        Iterator<Trip> itr = listOfTrips_return.iterator();

        while(itr.hasNext()) {
            if(Integer.getInteger((tripNumber)) == it.next().getTripID()) {
                query += it.next().toJSONText();
                break;
            }
        }

        return new JSONRPC2Response(query, id);
    }

    public static JSONRPC2Response reserveTrip(List<Flight> reservation,
                                               String typeOfSeat,
                                               Object id) {
        String query = "Seat Reserved Successfully";

        for(Flight flight : reservation) {
            String xmlReservation = "<Flights>"
                    + "<Flight number=\"" + flight.getmNumber() + "\" seating=\"Coach\"/>"
                    + "</Flights>";
            sys.lock(TEAM_DB);
            sys.buyTickets(TEAM_DB, xmlReservation);
            sys.unlock(TEAM_DB);
        }

        // Verify the operation worked
        for(Flight flight : reservation) {
            String xmlFlights = sys.getFlights(TEAM_DB, flight.getmCodeDepart(), flight.getmTimeDepart());
            check.clear();
            check.addAll(xmlFlights);
            int seatsReservedStart = getSeats(flight, typeOfSeat);

            int seatsReservedEnd = seatsReservedStart;

            for (Flight f : check) {
                seatsReservedStart = getSeats(f, typeOfSeat);
                // Find the flight number just updated
                seatsReservedEnd = seatsReservedStart;
                String tmpFlightNumber = f.getmNumber();
                if (tmpFlightNumber.equals(flight.getmNumber())) {
                    seatsReservedEnd = getSeats(flight, typeOfSeat);
                    break;
                }
            }

            if (seatsReservedEnd == (seatsReservedStart + 1)) {
                continue;
            } else {
                query = "Reservation Failed";
                break;
            }
        }

        return new JSONRPC2Response(query, id);
    }

    public static int getSeats(Flight flight, String typeOfSeat) {
        if(typeOfSeat.equals("Coach")) {
            return flight.getmSeatsCoach();
        }
        return flight.getmSeatsFirstclass();
    }
}
