import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.Date;
import java.util.HashMap;


/**
 * Created by eason on 2/26/17.
 */
public class Operation {
    private static ServerInterface sys = new ServerInterface();
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

    private static Trips listOfTrips = new Trips();
    private static Flights check = new Flights();
    private static int tripID;

    public static JSONRPC2Response process(String depAIR,
                                           String arrAIR,
                                           String depTime,
                                           String retTime,
                                           Object id) {


        // Valid input check
        if(depAIR == null || arrAIR == null || depTime == null) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, id);
        }

        String query = searchHelper(depAIR, arrAIR, depTime);
        String query_return = null;
        if(retTime != null) {
            query_return = searchHelper(arrAIR, depAIR, retTime);
        }

        return new JSONRPC2Response(query + "\n" + query_return, id);
    }

    private static String searchHelper(String depAIR, String arrAIR, String depTime) {

        String flight_leg1;

        HashMap<Flight, Flights> search_two = new HashMap<Flight, Flights>();
        HashMap<Flights, Flights> search_three = new HashMap<Flights, Flights>();

        listOfTrips.clear();
        tripID = 0;
        //Search the DB: the real logic part
        flight_leg1 = sys.getFlights(TEAM_DB, depAIR, depTime);
        Flights search_one = new Flights();
        search_one.addAll(flight_leg1);

        if(search_one.isEmpty()) {
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
                    search_second.addAll(sys.getFlights(TEAM_DB, f.getmCodeArrival(), arrival));
                    search_second_2.addAll(sys.getFlights(TEAM_DB, f.getmCodeArrival(), arr_next));
                    search_second.addAll(search_second_2);
                    search_two.put(f, search_second);
                }
            }
        }

        for(Flight f : search_two.keySet()) {
            Flights flights = search_two.get(f);
            for(Flight f_s : flights) {
                if(f_s.isValid() && isWithinLayover(f.getmTimeArrival(), f_s.getmTimeDepart())) {
                    Trip li = new Trip();
                    li.setTripID(tripID);
                    li.add(f);
                    li.add(f_s);
                    tripID ++;
                    if (f_s.getmCodeArrival().equals(arrAIR)) {
                        // For 1 leg flight it will be added to the result listOfTrips when the second flight has the arrAIR code the same as the destination
                        listOfTrips.append(li);
                    } else {
                        String arrival = toTime(f_s.getmTimeArrival());
                        String arr_next = getNextDay(arrival);
                        Flights search_third = new Flights();
                        Flights search_third_2 = new Flights();
                        search_third.addAll(sys.getFlights(TEAM_DB, f_s.getmCodeArrival(), arrival));
                        search_third_2.addAll(sys.getFlights(TEAM_DB, f_s.getmCodeArrival(), arr_next));
                        search_third.addAll(search_third_2);
                        search_three.put(li, search_third);
                    }
                }
            }
        }

        for(Flights f : search_three.keySet()) {
            Flights flights = search_three.get(f);
            Flight f_s = flights.get(1);
            for(Flight f_t : flights) {
                if (f_t.isValid()
                        && f_t.getmCodeArrival().equals(arrAIR)
                        && isWithinLayover(f_s.getmTimeArrival(), f_t.getmTimeDepart())) {
                    Trip fin = new Trip();
                    fin.setTripID(tripID);
                    fin.addAll(flights);
                    fin.add(f_t);
                    listOfTrips.append(fin);
                    tripID++;
                }
            }
        }

        if(listOfTrips.isEmpty()) {
            return null;
        }

        return listOfTrips.toJSONText();
    }

    /*
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

    */

    /*
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
    } */

    public static JSONRPC2Response reserveTrip(Trip reservation,
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
