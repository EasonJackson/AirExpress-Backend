import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.Date;
import java.util.HashMap;


/**
 * Created by eason on 2/26/17.
 */
public class Operation {
    private static final ServerInterface sys = new ServerInterface();
    private static final String TEAM_DB = "WickedSmaht";
    private static final double minLayover = 0.5;
    private static final double maxLayover = 3;
    private static final HashMap<String, String> MonthSwitch = new HashMap<String, String>();
    static {
        MonthSwitch.put("January", "01");
        MonthSwitch.put("February", "02");
        MonthSwitch.put("March", "03");
        MonthSwitch.put("April", "04");
        MonthSwitch.put("May", "05");
        MonthSwitch.put("June", "06");
        MonthSwitch.put("July", "07");
        MonthSwitch.put("August", "08");
        MonthSwitch.put("September", "09");
        MonthSwitch.put("October", "10");
        MonthSwitch.put("November", "11");
        MonthSwitch.put("December", "12");
    }

    private static final Airports AIRPORT = new Airports();
    static {
        AIRPORT.addAll(sys.getAirports(TEAM_DB));
    }

    private static Trips listOfTrips = new Trips();
    private static Flights check = new Flights();
    private static int tripID;

    public static JSONRPC2Response process(String depAIR,
                                           String arrAIR,
                                           String depTime,
                                           String retTime,
                                           Object id) {


        // Valid input check
        if(depAIR == null || arrAIR == null || depTime == null ||
                !AIRPORT.contains(depAIR) || !AIRPORT.contains(arrAIR)) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, id);
        }

        System.out.println("Process function gets called. Working ...");
        String query = searchHelper(depAIR, arrAIR, depTime);
        String query_return = null;
        if(retTime != null) {
            query_return = searchHelper(arrAIR, depAIR, retTime);
        }

        try {
            return JSONRPC2Response.parse("{\"result\":" + "[" + query + query_return + "]"+ ",\"id\": " +id +",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
        }
        return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, id);
    }

    private static String searchHelper(String depAIR, String arrAIR, String depTime) {

        String flight_leg1;
        String flight_leg3;

        HashMap<Flight, Flights> search_two = new HashMap<Flight, Flights>();
        HashMap<Flights, Flights> search_three = new HashMap<Flights, Flights>();

        listOfTrips.clear();
        tripID = 0;
        //Search the DB: the real logic part

        //First leg
        flight_leg1 = sys.getFlightsDeparting(TEAM_DB, depAIR, depTime);
        Flights search_one = new Flights();
        search_one.addAll(flight_leg1);

        System.out.println("Leg 1 search completed. Working on next stage ...");

        //Third leg
        flight_leg3 = sys.getFlightsArriving(TEAM_DB, arrAIR, depTime);
        Flights search_three_1 = new Flights();
        search_three_1.addAll(flight_leg3);
        flight_leg3 = sys.getFlightsArriving(TEAM_DB, arrAIR, getNextDay(depTime));
        Flights search_three_2 = new Flights();
        search_three_2.addAll(flight_leg3);
        search_three_1.addAll(search_three_2);

        System.out.println("Leg 3 search completed. Working on next stage ...");

        if(search_one.isEmpty() || search_three.isEmpty()) {
            return null;
        }

        for(Flight f : search_one) {
            if(f.isValid()) {
                if (f.getmCodeArrival().equals(arrAIR)) {
                    // For direct flight it will be added straight to the result listOfTrips
                    Trip li = new Trip();
                    li.setTripID(tripID);
                    li.add(f);
                    listOfTrips.append(li); // Adding direct flight to the listOfTrips
                    tripID ++;
                } else {
                    // Search for the second leg and store the result temporarily in the HashMap search_two
                    String arrival = toTime(f.getmTimeArrival());
                    String arr_next = getNextDay(arrival);
                    Flights search_second = new Flights();
                    Flights search_second_2 = new Flights();
                    search_second.addAll(sys.getFlightsDeparting(TEAM_DB, f.getmCodeArrival(), arrival));
                    search_second_2.addAll(sys.getFlightsDeparting(TEAM_DB, f.getmCodeArrival(), arr_next));
                    search_second.addAll(search_second_2);
                    search_two.put(f, search_second);
                }
            }
        }

        System.out.println("Leg 2 search complete. Working on next stage ...");

        for(Flight f : search_two.keySet()) {
            Flights flights = search_two.get(f);
            for(Flight f_s : flights) {
                if(f_s.isValid()) {
                    if (f_s.getmCodeArrival().equals(arrAIR)
                            && isWithinLayover(f.getmTimeArrival(), f_s.getmTimeDepart())) {
                        // For 1 leg flight it will be added to the result listOfTrips when the second flight has the arrAIR code the same as the destination
                        Trip li = new Trip();
                        li.setTripID(tripID);
                        li.add(f);
                        li.add(f_s);
                        listOfTrips.append(li);
                        tripID ++;
                    } else {
                        for(Flight f_t : search_three_1) {
                            // For 2 legs flight it will be added to the result of the f_s.ariAIR == f_t.depAIR and layover is reasonable
                            if(f_s.getmCodeArrival().equals(f_t.getmCodeDepart())
                                    && isWithinLayover(f_s.getmTimeArrival(), f_t.getmTimeDepart())) {
                                Trip li = new Trip();
                                li.setTripID(tripID);
                                li.add(f);
                                li.add(f_s);
                                li.add(f_t);
                                listOfTrips.append(li);
                                tripID ++;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Search completed. Returning results ...");


        if(listOfTrips.isEmpty()) {
            return null;
        }

        return listOfTrips.toJSONText();
    }

    // Test case for process: a simply method for receiving query from db
    public static JSONRPC2Response processSimple(String depAIR,
                                                 String arrAIR,
                                                 String depTime,
                                                 String retTime,
                                                 Object id) {
        String xmlFlights = sys.getFlightsDeparting(TEAM_DB, depAIR, depTime);
        Trip f = new Trip();
        f.addAll(xmlFlights);
        Trips t = new Trips();
        t.add(f);
        try {
            return JSONRPC2Response.parse("{\"result\":" + t.toJSONText() + ",\"id\": " +id +",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
            return new JSONRPC2Response(t.toJSONText(), id);
        }
    }

    public static JSONRPC2Response reserveTrip(Trip reservation,
                                               String typeOfSeat,
                                               Object id) {

        System.out.println("reserveTrip function gets called. Working ...");
        String query = reserveHelper(reservation, typeOfSeat, id);

        try {
            return JSONRPC2Response.parse("{\"result\":" + query + ",\"id\": " + id + ",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
        }
        return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, id);
    }

    private static String reserveHelper(Trip reservation,
                                        String typeOfSeat,
                                        Object id) {

        for(Flight flight : reservation) {
            String xmlReservation = "<Flights>"
                    + "<Flight number=\"" + flight.getmNumber() + "\" seating=\""
                    + typeOfSeat + "\"/>" + "</Flights>";
            sys.lock(TEAM_DB);
            sys.buyTickets(TEAM_DB, xmlReservation);
            sys.unlock(TEAM_DB);
        }

        System.out.println("Database updated. Checking the reservation information ...");

        // Verify the operation worked
        for(Flight flight : reservation) {
            String xmlFlights = sys.getFlightsDeparting(TEAM_DB, flight.getmCodeDepart(), flight.getmTimeDepart());
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
                return "Reservation Failed";
            }
        }

        System.out.println("Reservation confirmed. All set.");
        return "Seat Reserved Successfully";
    }

    private static int getSeats(Flight flight, String typeOfSeat) {
        if(typeOfSeat.equals("Coach")) {
            return flight.getmSeatsCoach();
        }
        return flight.getmSeatsFirstclass();
    }

    private static boolean isWithinLayover(String arr, String dep) {
        Date depD = new Date(Integer.parseInt(dep.split("\\s")[0].trim()),
                             Integer.parseInt(MonthSwitch.get(dep.split(" ")[1].trim())),
                             Integer.parseInt(dep.split("\\s")[2].trim()),
                             Integer.parseInt(dep.split("\\s")[3].trim().split(":")[0].trim()),
                             Integer.parseInt(dep.split("\\s")[3].trim().split(":")[1].trim()));
        Date arrD = new Date(Integer.parseInt(arr.split("\\s")[0].trim()),
                             Integer.parseInt(MonthSwitch.get(arr.split("\\s")[1].trim())),
                             Integer.parseInt(arr.split("\\s")[2].trim()),
                             Integer.parseInt(dep.split("\\s")[3].trim().split(":")[0].trim()),
                             Integer.parseInt(dep.split("\\s")[3].trim().split(":")[1].trim()));
        double delay = (depD.getTime() - arrD.getTime()) / 3600000;
        if(delay > maxLayover || delay < minLayover) {
            return false;
        }
        return true;
    }

    private static String getNextDay(String date) {
        Date d = new Date(Integer.parseInt(date.split("_")[0].trim()),
                          Integer.parseInt(date.split("_")[1].trim()),
                          Integer.parseInt(date.split("_")[2].trim()));
        Date nextDay = new Date(d.getTime() + 86400000);
        return "" +  nextDay.getYear() + "_" + String.format("%02d",nextDay.getMonth()) + "_" + String.format("%02d", nextDay.getDate());
    }

    private static String toTime(String Time) {
        String[] dataForm = Time.split(" ");
        return dataForm[0].trim() + '_' + MonthSwitch.get(dataForm[1].trim()) + '_' + dataForm[2].trim();
    }

}
