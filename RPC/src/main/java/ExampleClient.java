import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;

/**
 * Created by eason on 2/23/17.
 */
public class ExampleClient {

    private static URL serverURL;
    private static JSONRPC2Session session;

    public ExampleClient() {
        try {
            serverURL = new URL("http://localhost:8080");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        session = new JSONRPC2Session(serverURL);
        session.getOptions().setConnectTimeout(5000);
        session.getOptions().setReadTimeout(1000);
    }

    public JSONRPC2Response searchFlight(String method, List<Object> params) {
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new java.util.Date());
        int requestID = date.hashCode();
        JSONRPC2Request req = new JSONRPC2Request(method, params, requestID);
        System.out.println(req);

        return sendRequest(req);
    }

    public JSONRPC2Response sortByPrice(String method) {
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new java.util.Date());
        int requestID = date.hashCode();
        JSONRPC2Request req = new JSONRPC2Request(method, requestID);
        System.out.println(req);

        return sendRequest(req);
    }


    public JSONRPC2Response sortByDuration(String method) {
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new java.util.Date());
        int requestID = date.hashCode();
        JSONRPC2Request req = new JSONRPC2Request(method, requestID);
        System.out.println(req);

        return sendRequest(req);
    }

    public JSONRPC2Response reserveFlight(String method, List<Object> params) {
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new java.util.Date());
        int requestID = date.hashCode();
        JSONRPC2Request req = new JSONRPC2Request(method, params, requestID);
        System.out.println(req);

        return sendRequest(req);
    }

    public static JSONRPC2Response sendRequest(JSONRPC2Request req) {
        JSONRPC2Response resp = null;
        try {
            resp = session.send(req);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }

        if(resp.indicatesSuccess()) {
            System.out.println(resp.getResult());
        } else {
            System.out.println(resp.getError().getMessage());
        }
        return resp;
    }
}
