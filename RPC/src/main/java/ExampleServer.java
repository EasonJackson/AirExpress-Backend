/**
 * Created by eason on 2/21/17.
 */
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.*;
import java.text.*;

public class ExampleServer {
    private static final int SERVER_PORT = 8080;

    //private static ServerInterface sys = new ServerInterface();
    //private static final HashMap<String, String> airport = xmlParser(sys.getAirports(TEAM_DB));


    //Test echo method
    public static class EchoHandler implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "echo"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

            if(req.getMethod().equals("echo")) {
                List params = (List) req.getParams();
                Object input = params.get(0);
                return new JSONRPC2Response(input, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    public static class DateTimeHandler implements RequestHandler {
        public String[] handledRequests() {
            return new String[]{
                    "getDate", "getTime"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if(req.getMethod().equals("getDate")) {
                DateFormat df = DateFormat.getDateInstance();
                String date = df.format(new Date());
                return new JSONRPC2Response(date, req.getID());
            } else if(req.getMethod().equals(("getTime"))) {
                Timestamp tm = new Timestamp(System.currentTimeMillis());
                String time = tm.toString();
                return new JSONRPC2Response(time, req.getID());

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    //SearchFlight request handler
    public static class SearchFlight implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "searchFlight"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if(req.getMethod().equals("searchFlight")) {
                List params = (List) req.getParams();
                String depAIR = (String) params.get(0);
                String arrAIP = (String) params.get(1);
                String depTime = (String) params.get(2);
                String retTime = (String) params.get(3);

                return Operation.process(depAIR, arrAIP, depTime, retTime, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    //SortFlight
    /*
    public static class SortFlight implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "sortByPrice", "sortByDuration"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if(req.getMethod().equals("sortByPrice")) {
                return Operation.sortByPrice(req.getID());
            } else if(req.getMethod().equals("sortByDuration")) {
                return Operation.sortByDuration(req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }
    */

    //ReserveTrip
    public static class ReserveTrip implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "reserveTrip"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if (req.getMethod().equals("reserve")) {
                List params = (List) req.getParams();
                Trip reservation = (Trip) params.get(0);
                reservation.addAll((List<Flight>) params.get(1));
                String typeOfSeat = (String) params.get(2);
                return Operation.reserveTrip(reservation, typeOfSeat, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    /*
    public static class GetDetails implements RequestHandler {
        public String[] handledRequests() {
            return new String[]{
                    "getFlightDetails"
            };
        }
        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if(req.getMethod().equals("getFlightDetails")) {
                List params = (List) req.getParams();
                String flightNumber = (String) params.get(0);
                return Operation.getFlightDetails(flightNumber, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }
    */

    public static void main(String[] args) throws IOException{

        Dispatcher dispatcher = new Dispatcher();

        dispatcher.register(new EchoHandler());
        dispatcher.register(new DateTimeHandler());
        dispatcher.register(new SearchFlight());
        //dispatcher.register(new SortFlight());
        dispatcher.register(new ReserveTrip());
        //dispatcher.register(new GetDetails());

        /*
        List echoParam = new LinkedList();
        echoParam.add("Hello world");

        JSONRPC2Request req = new JSONRPC2Request("echo", echoParam, "req-id-01");
        System.out.println("Request: \n" + req);

        JSONRPC2Response resp = dispatcher.process(req, null);
        System.out.println("Response: \n" + resp);

        req = new JSONRPC2Request("getDate", "req-id-02");
        System.out.println("Request: \n" + req);

        resp = dispatcher.process(req, null);
        System.out.println("Response: \n" + resp);

        req = new JSONRPC2Request("getTime", "req-id-03");
        System.out.println("Request: \n" + req);

        resp = dispatcher.process(req, null);
        System.out.println("Response: \n" + resp);
        */

        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/", new MyHandler(dispatcher));
        server.setExecutor(null);
        server.start();


        //JSONRPC2Response resp = dispatcher.process(jsonReq, null);
        //System.out.println("Request: \n" + jsonReq);
        //System.out.println("Response: \n" + resp);

    }


    static class MyHandler implements HttpHandler {

        private Dispatcher dispatcher;
        private JSONRPC2Request jsonReq;
        private JSONRPC2Response jsonResp;

        public MyHandler(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        public void handle(HttpExchange t) throws IOException {
            //Print out the HTTP request type
            System.out.println("Http request: \n" + t.getRequestMethod());
            //URI requestURI = t.getRequestURI();

            //Print out the request body
            InputStreamReader rd = new InputStreamReader(t.getRequestBody());
            BufferedReader brd = new BufferedReader(rd);
            int b;
            StringBuilder sb = new StringBuilder(512);
            while((b = brd.read()) != -1) {
                sb.append((char) b);
            }
            brd.close();
            rd.close();
            String jsonString = sb.toString();
            System.out.println(jsonString);

            //Try to parse the jsonReq
            try {
                jsonReq = JSONRPC2Request.parse(jsonString);
            } catch (Exception e) {
               e.printStackTrace();
            }

            jsonResp = dispatcher.process(jsonReq, null);
            System.out.println("JSON response: " + jsonResp);

            /*String response = "HEllo"; */ //Test response
            //Send back the response
            t.getResponseHeaders().set("Content-Type", "text/plain");
            t.sendResponseHeaders(200, jsonResp.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(jsonResp.toString().getBytes());
            os.close();
            //String query = requestURI.getRawQuery();
        }

    }

    public static HashMap<String, String> xmlParser(String pattern) {
        HashMap<String, String> result = new HashMap<String, String>();
        return result;
    }
}
