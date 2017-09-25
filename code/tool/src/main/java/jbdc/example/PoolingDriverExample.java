package jbdc.example;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.*;
import java.util.Arrays;

/**
 * Created by xiaoxuez on 2017/9/1.
 */
public class PoolingDriverExample {
    public static void main(String[] args) {
        //
        // First we load the underlying JDBC driver.
        // You need this if you don't use the jdbc.drivers
        // system property.
        //
        System.out.println("Loading underlying JDBC driver.");
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Done.");

        //
        // Then we set up and register the PoolingDriver.
        // Normally this would be handled auto-magically by
        // an external configuration, but in this example we'll
        // do it manually.
        //
        System.out.println("Setting up driver.");
        try {
            setupDriver("jdbc:mysql://localhost:3306/storm?useSSL=false");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done.");

        //
        // Now, we can use JDBC as we normally would.
        // Using the connect string
        //  jdbc:apache:commons:dbcp:example
        // The general form being:
        //  jdbc:apache:commons:dbcp:<name-of-pool>
        //

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try {
            System.out.println("Creating connection.");
            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:example");
            System.out.println("Creating statement.");
            stmt = conn.createStatement();
            System.out.println("Executing statement.");
            stmt.execute("INSERT INTO  airguru_test (name, age) VALUES ('xiaoming', 2);");
            System.out.println("Results:");
            int numcols = rset.getMetaData().getColumnCount();
//            while(rset.next()) {
//                for(int i=1;i<=numcols;i++) {
//                    System.out.print("\t" + rset.getString(i));
//                }
//                System.out.println("");
//            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rset != null) rset.close(); } catch(Exception e) { }
            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
            try { if (conn != null) conn.close(); } catch(Exception e) { }
        }

        // Display some pool statistics
        try {
            printDriverStats();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // closes the pool
        try {
            shutdownDriver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setupDriver(String connectURI) throws Exception {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectURI,"root", "");

        //
        // Next, we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself...
        //
        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

        //
        // ...and register our pool with it.
        //
        driver.registerPool("example",connectionPool);

        //
        // Now we can just use the connect string "jdbc:apache:commons:dbcp:example"
        // to access our pool of Connections.
        //
    }

    public static void printDriverStats() throws Exception {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        ObjectPool<? extends Connection> connectionPool = driver.getConnectionPool("example");

        System.out.println("NumActive: " + connectionPool.getNumActive());
        System.out.println("NumIdle: " + connectionPool.getNumIdle());
    }

    public static void shutdownDriver() throws Exception {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.closePool("example");
    }
}
