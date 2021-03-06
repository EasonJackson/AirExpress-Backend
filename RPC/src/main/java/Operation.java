import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.model.LatLng;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.*;


/**
 * Created by eason on 2/26/17.
 */
public class Operation {
    private static final ServerInterface sys = new ServerInterface();
    private static final String TEAM_DB = "WickedSmaht";
    private static final double minLayover = 0.5 * 3600000;
    private static final double maxLayover = 4 * 3600000;
    private static final HashMap<String, String> MonthSwitch = new HashMap<String, String>();
    static {
        MonthSwitch.put("Jan", "01");
        MonthSwitch.put("Feb", "02");
        MonthSwitch.put("Mar", "03");
        MonthSwitch.put("Apr", "04");
        MonthSwitch.put("May", "05");
        MonthSwitch.put("Jun", "06");
        MonthSwitch.put("Jul", "07");
        MonthSwitch.put("Aug", "08");
        MonthSwitch.put("Sep", "09");
        MonthSwitch.put("Oct", "10");
        MonthSwitch.put("Nov", "11");
        MonthSwitch.put("Dec", "12");
    }

    private static final Airports AIRPORTS;
    private static final HashSet<String> AIRPORTS_CODE = new HashSet<String>();
    static {
        AIRPORTS = new Airports();
        AIRPORTS.addAll(sys.getAirports(TEAM_DB));
        for(Airport airport : AIRPORTS) {
            AIRPORTS_CODE.add(airport.code());
        }
    }

    private static final HashMap<String, Airplane> AIRPLANES = new HashMap<String, Airplane>();
    static {
        Airplanes airplanes = new Airplanes();
        airplanes.addAll(sys.getAirplanes(TEAM_DB));
        for(Airplane airplane : airplanes) {
            AIRPLANES.put(airplane.model(), airplane);
        }
    }

    private static String AIRPORTS_QUERY;
    private static Trips listOfTrips = new Trips();
    private static Flights check = new Flights();
    private static int tripID;

    private Operation() {}

    // getAirports
    public static JSONRPC2Response getAirports(Object id) {
        if(AIRPORTS_QUERY != null) {
            try {
                return JSONRPC2Response.parse("{\"result\": " + AIRPORTS_QUERY + ",\"id\": \"" + id + "\",\"jsonrpc\":\"2.0\"}");
            } catch (JSONRPC2ParseException e) {
                e.printStackTrace();
            }
        }
        GeoApiContext ctx = new GeoApiContext().setApiKey("AIzaSyAmGNL2f_7a172eqp4YPnmTU-eqQFzWcNk")
                .setQueryRateLimit(3);
        TimeZone tz;
        LatLng location;
        AIRPORTS_QUERY = "[";
        Iterator<Airport> iter = AIRPORTS.iterator();
        while(iter.hasNext()) {
            Airport airport = iter.next();
            long offset = 0;
            location = new LatLng(airport.latitude(), airport.longitude());
            try {
                tz = TimeZoneApi.getTimeZone(ctx, location).await();
                offset = tz.getRawOffset() + tz.getDSTSavings();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(airport.code().equals("SJC")) {
                airport.name("Mineta San Jose International");
            }
            AIRPORTS_QUERY += "{ \"Name\": " + "\"" + airport.name() + "\"," +
                    "\"Code\": " + "\"" + airport.code() + "\"," +
                    "\"Latitude\": " + airport.latitude() + "," +
                    "\"Longitude\": " + airport.longitude() + "," +
                    "\"Offset\": "+ offset + "}";
            if(iter.hasNext()) {
                AIRPORTS_QUERY += ",";
            }
        }
        AIRPORTS_QUERY += "]";

        try {
            return JSONRPC2Response.parse("{\"result\": " + AIRPORTS_QUERY + ",\"id\": \"" + id + "\",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
        }
        return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, id);
    }

    // SearchFlight
    public static JSONRPC2Response process(String depAIR,
                                           String arrAIR,
                                           String depTime,
                                           String retTime,
                                           Object id) {
        // Valid input check

        if(depAIR == null || arrAIR == null || depTime == null ||
                !AIRPORTS_CODE.contains(depAIR) || !AIRPORTS_CODE.contains(arrAIR)) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, id);
        }


        System.out.println("Process function gets called. Working ...");
        String query = searchHelper(depAIR, arrAIR, depTime);
        String query_return = "[]";
        if(retTime != null && !retTime.equals("")) {
            System.out.println("Return is not null");
            query_return = searchHelper(arrAIR, depAIR, retTime);
        }
        System.out.println("Search finished. Starting parsing to JSON text ...");

        try {
            return JSONRPC2Response.parse("{\"result\":{\"depart\":" + query + ",\"return\":" + query_return + "},\"id\": \"" +id +"\",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
        }
        return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, id);
    }

    private static String searchHelper(String depAIR, String arrAIR, String depTime) {

        String flight_leg1;
        String flight_leg3;

        HashMap<Flight, Flights> search_two = new HashMap<Flight, Flights>();

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
        Flights search_three = new Flights();
        search_three.addAll(flight_leg3);
        flight_leg3 = sys.getFlightsArriving(TEAM_DB, arrAIR, getNextDay(depTime));
        Flights search_three_2 = new Flights();
        search_three_2.addAll(flight_leg3);
        search_three.addAll(search_three_2);
        flight_leg3 = sys.getFlightsArriving(TEAM_DB, arrAIR, getNextDay(getNextDay(depTime)));
        Flights search_three_3 = new Flights();
        search_three_3.addAll(flight_leg3);
        search_three.addAll(search_three_3);
        System.out.println("Leg 3 search completed. Working on next stage ...");

        if(search_one.isEmpty() || search_three.isEmpty()) {
            System.out.println("No flight from depart airport OR no flight to arriving airport. ");
            return "[]";
        }

        for(Flight f : search_one) {
            if(f.isValid()) {
                if (f.getmCodeArrival().equals(arrAIR)) {
                    // For direct flight it will be added straight to the result listOfTrips
                    Trip li = new Trip();
                    li.setTripID(tripID);
                    li.add(f);
                    listOfTrips.append(li);
                    tripID ++;
                    System.out.println("Trip added to results." + tripID);
                } else {
                    // Search for the second leg and store the result temporarily in the HashMap search_two
                    String arrival = toTime(f.getmTimeArrival());
                    String arr_next = getNextDay(arrival);
                    Flights search_second = new Flights();
                    Flights search_second_2 = new Flights();
                    search_second.addAll(sys.getFlightsDeparting(TEAM_DB, f.getmCodeArrival(), arrival));
                    search_second_2.addAll(sys.getFlightsDeparting(TEAM_DB, f.getmCodeArrival(), arr_next));
                    search_second.addAll(search_second_2);
                    if(!search_second.isEmpty()) {
                        search_two.put(f, search_second);
                    }
                }
            }
        }

        //System.out.println(search_two.isEmpty());

        System.out.println("Leg 2 search complete. Working on next stage ...");

        for(Flight f : search_two.keySet()) {
            Flights flights = search_two.get(f);
            for(Flight f_s : flights) {
                //System.out.println(f.getmTimeArrival() + f_s.getmTimeDepart() + " " + isWithinLayover(f.getmTimeArrival(), f_s.getmTimeDepart()));
                if(f_s.isValid() && isWithinLayover(f.getmTimeArrival(), f_s.getmTimeDepart())) {
                    if (f_s.getmCodeArrival().equals(arrAIR)) {
                        // For 1 leg flight it will be added to the result listOfTrips when the second flight has the arrAIR code the same as the destination
                        Trip li = new Trip();
                        li.setTripID(tripID);
                        li.add(f);
                        li.add(f_s);
                        listOfTrips.append(li);
                        System.out.println("Trip added to results." + tripID);
                        tripID ++;
                    } else {
                        for(Flight f_t : search_three) {
                            // For 2 legs flight it will be added to the result of the f_s.ariAIR == f_t.depAIR and layover is reasonable
                            if(f_s.getmCodeArrival().equals(f_t.getmCodeDepart())
                                    && isWithinLayover(f_s.getmTimeArrival(), f_t.getmTimeDepart())) {
                                Trip li = new Trip();
                                li.setTripID(tripID);
                                li.add(f);
                                li.add(f_s);
                                li.add(f_t);
                                listOfTrips.append(li);
                                System.out.println("Trip added to results." + tripID);
                                tripID ++;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Search completed. Returning results ...");

        if(listOfTrips.isEmpty()) {
            return "[]";
        }

        String q = listOfTrips.toJSONText();

        return q;
    }

    // Test case for searchFlight: a simply method for receiving query from db
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
            return JSONRPC2Response.parse("{\"result\":" + t.toJSONText() + ",\"id\": \"" +id +"\",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
            return new JSONRPC2Response(t.toJSONText(), id);
        }
    }

    // reserveTripTest
    public static JSONRPC2Response reserveTripSimple(String reservation,
                                                     String typeOfSeat,
                                                     Object id) {
        List<String> reserve;
        reserve = Arrays.asList(reservation.split(","));
        String query = typeOfSeat;
        for(String trip : reserve) {
            query += "Flight #" + trip.split(" ")[0] + ":";
            query += trip.split(" ")[1] + " + ";
            query += trip.split(" ")[2];
        }
        return new JSONRPC2Response(query, id);
    }

    // reserveTrip
    public static JSONRPC2Response reserveTrip(String reservation,
                                               String typeOfSeat,
                                               Object id) {

        System.out.println("reserveTrip function gets called. Working ...");
        List<String> reserve;
        reserve = Arrays.asList(reservation.split(","));
        String query = reserveHelper(reserve, typeOfSeat);
        System.out.println("{\"result\":" + query + ",\"id\": \"" + id + "\",\"jsonrpc\":\"2.0\"}");

        try {
            return JSONRPC2Response.parse("{\"result\": \"" + query + "\",\"id\": \"" + id + "\",\"jsonrpc\":\"2.0\"}");
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
        }
        return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, id);
    }

    private static String reserveHelper(List<String> reservation,
                                        String typeOfSeat) {
        check.clear();
        // Availability check
        sys.lock(TEAM_DB);
        for(String flightSt : reservation) {
            String flightNumber = flightSt.split(" ")[0].trim();
            String CodeDepart = flightSt.split(" ")[1].trim();
            String TimeDepart = flightSt.split(" ")[2].trim();

            String xmlFlights = sys.getFlightsDeparting(TEAM_DB, CodeDepart, TimeDepart);
            Flights temp = new Flights();
            temp.addAll(xmlFlights);

            for (Flight f : temp) {
                String tmpFlightNumber = f.getmNumber();
                if (tmpFlightNumber.equals(flightNumber)) {
                    int seatsAva = typeOfSeat.equals("Coach") ? AIRPLANES.get(f.getmAirplane()).coachSeats() : AIRPLANES.get(f.getmAirplane()).firstClassSeats();
                    if(getSeats(f, typeOfSeat) >= seatsAva) {
                        sys.unlock(TEAM_DB);
                        return "Reservation fails: Seats not available.";
                    }
                    check.add(f);
                    break;
                }
            }
        }

        // Reserve the flights
        for(Flight f : check) {
            String xmlReservation = "<Flights>"
                    + "<Flight number=\"" + f.getmNumber() + "\" seating=\""
                    + typeOfSeat + "\"/>" + "</Flights>";
            sys.buyTickets(TEAM_DB, xmlReservation);
        }
        sys.unlock(TEAM_DB);
            System.out.println("Database updated. Checking the reservation information ...");

        // Verify the operation worked
        List<Integer> seatsReservedEnd = new ArrayList<Integer>();
        for(Flight flight : check) {
            int seatsReservedStart = getSeats(flight, typeOfSeat);
            //int seatsReservedEnd = seatsReservedStart;

            String xmlFlights = sys.getFlightsDeparting(TEAM_DB, flight.getmCodeDepart(), toTime(flight.getmTimeDepart()));
            Flights temp = new Flights();
            temp.addAll(xmlFlights);

            for (Flight f : temp) {
                String tmpFlightNumber = f.getmNumber();
                if (tmpFlightNumber.equals(flight.getmNumber())) {
                    seatsReservedEnd.add(getSeats(f, typeOfSeat));
                    break;
                }
            }

            if (seatsReservedEnd.get(seatsReservedEnd.size() - 1) == (seatsReservedStart + 1)) {
                continue;
            } else {
                return "Reservation Failed: database updates error.";
            }
        }
        for(int i = 0; i < check.size(); i++) {
            System.out.println(check.get(i).getmNumber() + " " + typeOfSeat + "Started at:" + check.get(i).getmSeatsCoach() + "Ended with" + seatsReservedEnd.get(i));
        }
        System.out.println("Reservation confirmed. All set.");
        return "Seat Reserved Successfully";
    }


    //
    private static int getSeats(Flight flight, String typeOfSeat) {
        if(typeOfSeat.equals("Coach")) {
            return flight.getmSeatsCoach();
        }
        return flight.getmSeatsFirstclass();
    }

    private static boolean isWithinLayover(String arr, String dep) {
        Calendar depC = Calendar.getInstance();
        depC.set(Calendar.YEAR, Integer.parseInt(dep.split("\\s")[0].trim()) + 1900);
        depC.set(Calendar.MONTH, Integer.parseInt(MonthSwitch.get(dep.split("\\s")[1].trim())));
        depC.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dep.split("\\s")[2].trim()));
        depC.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dep.split("\\s")[3].trim().split(":")[0].trim()));
        depC.set(Calendar.MINUTE, Integer.parseInt(dep.split("\\s")[3].trim().split(":")[1].trim()));
        Calendar arrC = Calendar.getInstance();
        arrC.set(Calendar.YEAR, Integer.parseInt(arr.split("\\s")[0].trim()) + 1900);
        arrC.set(Calendar.MONTH, Integer.parseInt(MonthSwitch.get(arr.split("\\s")[1].trim())));
        arrC.set(Calendar.DAY_OF_MONTH, Integer.parseInt(arr.split("\\s")[2].trim()));
        arrC.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr.split("\\s")[3].trim().split(":")[0].trim()));
        arrC.set(Calendar.MINUTE, Integer.parseInt(arr.split("\\s")[3].trim().split(":")[1].trim()));
        double delay = depC.getTimeInMillis() - arrC.getTimeInMillis();
        if(delay > maxLayover || delay < minLayover) {
            return false;
        }
        return true;
    }

    private static String getNextDay(String date) {
        Calendar C = Calendar.getInstance();
        C.set(Calendar.YEAR, Integer.parseInt(date.split("_")[0].trim()));
        C.set(Calendar.MONTH, Integer.parseInt(date.split("_")[1].trim()));
        C.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("_")[2].trim()));
        C.setTimeInMillis(C.getTimeInMillis() + 86400000);
        return "" +  C.YEAR + "_" + String.format("%02d",C.MONTH) + "_" + String.format("%02d", C.DAY_OF_MONTH);
    }

    private static String toTime(String Time) {
        String[] dataForm = Time.split(" ");
        return dataForm[0].trim() + '_' + MonthSwitch.get(dataForm[1].trim()) + '_' + dataForm[2].trim();
    }

}
