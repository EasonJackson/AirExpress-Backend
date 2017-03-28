import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eason on 3/1/17.
 */
public class BackendTestCase {

    public static void main(String[] args) {

        ExampleClient client = new ExampleClient();
        List<Object> params = new ArrayList<Object>();
        params.add((Object) "BOS");
        params.add((Object) "LGA");
        params.add((Object) "2017_05_10");
        params.add(null);


    }
}
