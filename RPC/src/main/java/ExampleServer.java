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
                Map<String, Object> map = (Map<String, Object>) req.getNamedParams();
                Object input = params.get(0);
                Set<String> input2 = map.keySet();
                String s = "";
                for(String st : input2) {
                    s += ((String[]) map.get(st))[0] + " " + ((String[]) map.get(st))[1] + "\t";
                }
                System.out.println(s);
                System.out.println("Test passed");
                return new JSONRPC2Response(input, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    //Test time and date method
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

    //Test db query for search method
    public static class SearchFlightTest implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "searchFlightTest"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if (req.getMethod().equals("searchFlightTest")) {
                List params = (List) req.getParams();
                Object depAIR = params.get(0);
                Object arrAIR = params.get(1);
                Object depTime = params.get(2);
                Object retTime = params.get(3);
                return Operation.processSimple((String)depAIR, (String)arrAIR, (String)depTime, (String)retTime, req.getID());

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
                Object depAIR =  params.get(0);
                Object arrAIR =  params.get(1);
                Object depTime = params.get(2);
                Object retTime = null;
                if(params.size() == 4) {
                    retTime = params.get(3);
                }
                return Operation.process((String)depAIR, (String)arrAIR, (String)depTime, (String)retTime, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    //ReserveTrip test case
    public static class ReserveTripSimple implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "reserveTripSimple"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if (req.getMethod().equals("reserverTripSimple")) {
                List params = (List) req.getParams();
                Map<String, Object> map = (Map<String, Object>) req.getNamedParams();
                Object typeOfSeats = params.get(0);
                for (String st : map.keySet()) {
                    System.out.println(st + " " + ((String[])map.get(st))[0] + " " + ((String[])map.get(st))[1]);
                }
                return new JSONRPC2Response(" ",req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    //ReserveTrip
    public static class ReserveTrip implements RequestHandler {
        public String[] handledRequests() {
            return new String[] {
                    "reserveTrip"
            };
        }

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if (req.getMethod().equals("reserve")) {
                Map<String, Object> map = req.getNamedParams();
                String typeOfSeat = "";
                LinkedList<String> reservation = null;
                try {
                    for(String key : map.keySet()) {
                        if(key.equals("typeOfSeat")) {
                            typeOfSeat = (String) map.get("typeOfSeat");
                        } else {
                            reservation.add(key + " " + ((String[]) map.get(key))[0].trim() + " " + ((String[]) map.get(key))[1].trim());
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return Operation.reserveTrip(reservation, typeOfSeat, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    public static void main(String[] args) throws IOException{

        Dispatcher dispatcher = new Dispatcher();

        dispatcher.register(new EchoHandler());
        dispatcher.register(new DateTimeHandler());
        dispatcher.register(new SearchFlight());
        dispatcher.register(new ReserveTrip());
        dispatcher.register(new SearchFlightTest());
        dispatcher.register(new ReserveTripSimple());

        List echoParam = new LinkedList();
        echoParam.add("Self testing query of List Param");
        Map<String, Object> echoMap = new HashMap<String, Object>();
        String[] s = {"Local", "Self tesing query of Named Param"};
        echoMap.put("001", s);
        echoMap.put("002", s);
        JSONRPC2Request req = new JSONRPC2Request("echo", "req-id-01");
        req.setPositionalParams(echoParam);
        req.setNamedParams(echoMap);
        System.out.println("Request: \n" + req);
        JSONRPC2Response resp = dispatcher.process(req, null);
        System.out.println(resp);

        /*
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
        System.out.println("RPC request dispatch server is running. Local port: " + SERVER_PORT);
    }


    private static class MyHandler implements HttpHandler {

        private Dispatcher dispatcher;
        private JSONRPC2Request jsonReq;
        private JSONRPC2Response jsonResp;

        MyHandler(Dispatcher dispatcher) {
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
}
