import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.storm.topology.FailedException;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.DriverManager;

public class Test {


    public static void main(String args[]) {
        Connection c = null;
//        try {
//            Class.forName("org.postgresql.Driver");
//            c = DriverManager
//                    .getConnection("jdbc:postgresql://localhost:5432/airguru",
//                            "storm", "");
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println(e.getClass().getName()+": "+e.getMessage());
//            System.exit(0);
//        }
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("airguru");
        source.setUser("storm");
        source.setPassword("");
        source.setMaxConnections(10);
        try {
            c = source.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Opened database successfully");
    }

    public class A {
        String name;
        Object a;
    }




}
