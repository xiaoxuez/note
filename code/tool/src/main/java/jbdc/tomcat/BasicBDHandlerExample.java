package jbdc.tomcat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by xiaoxuez on 2017/9/7.
 */
public class BasicBDHandlerExample {
    public static void main(String[] args) throws Exception {
        DataSource dataSource = new DataSource()
                .setJdbcUrl("jdbc:mysql://localhost:3306/storm?useSSL=false")
                .setName("local_sql")
                .setUser("root")
                .setPwd("");
        ConnectionPool.createConnectionPool(dataSource);
//        INSERT INTO  basic_test set name='12';

        BasicBDHandler basicBDHandler = new BasicBDHandler(dataSource.getName());
        basicBDHandler.executor("CREATE TABLE IF NOT EXISTS basic_test (ID INT(5) PRIMARY KEY AUTO_INCREMENT,name text, age int)");
        basicBDHandler.executorS("INSERT INTO  airguru_test (name, age) VALUES ('xiaoming', 2);");
//        basicBDHandler.executor((connection) -> {
//            try {
//                Statement statement = connection.createStatement();
//                statement.execute("INSERT INTO  basic_test (name, age) VALUES ('xiaoming', 1)");
//                System.out.println("======= insert ok!");
//                statement.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//
//        });

        Connection conn = ConnectionPool.getConnection(dataSource.getName());
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO  airguru_test (name, age) VALUES ('xiaoming', 2);");
        ConnectionPool.close(conn, stmt);
    }
}
