package jbdc.tomcat;

import jbdc.tomcat.ConnectionPool;
import jbdc.tomcat.DataSource;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by xiaoxuez on 2017/9/7.
 */
public class TomcatConnectionPoolExample {
    public static void main(String[] args) throws Exception {
        DataSource dataSource = new DataSource()
                .setJdbcUrl("jdbc:mysql://localhost:3306/storm?useSSL=false")
                .setName("local_sql")
                .setUser("root")
                .setPwd("");
        ConnectionPool.createConnectionPool(dataSource);
        Connection conn = ConnectionPool.getConnection(dataSource.getName());
        Statement stmt = conn.createStatement();
        System.out.println(stmt == null);
        stmt.execute("CREATE TABLE IF NOT EXISTS basic_test (ID INT(5) PRIMARY KEY AUTO_INCREMENT,name text, age int)");
        stmt.execute("INSERT INTO  basic_test (name, age) VALUES ('xiaoming', 2);");
        ConnectionPool.close(conn, stmt);

        GenericObjectPool pool = (GenericObjectPool) ConnectionPool.getConnectionPool(dataSource.getName());
        System.out.println("NumActive: " + pool.getNumActive());
        System.out.println("NumIdle: " + pool.getNumIdle());
        System.out.println("MaxActive: " + pool.getMaxActive());
        System.out.println("Maxidle: " + pool.getMaxIdle());


    }
}
