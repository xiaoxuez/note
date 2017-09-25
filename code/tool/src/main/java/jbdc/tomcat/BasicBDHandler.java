package jbdc.tomcat;


import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by xiaoxuez on 2017/9/7.
 */
public class BasicBDHandler {

    protected String name;
    public BasicBDHandler(String name) {
        this.name = name;
    }

    public Connection getConnection() throws Exception {
        return ConnectionPool.getConnection(name);
    }

    public void executor(String ...sentences) throws Exception {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        for (String sentence : sentences) {
            statement.execute(sentence);
        }
        ConnectionPool.close(connection, statement);
    }

    public void executor(Consumer<Connection> action) throws Exception {
        Connection connection = getConnection();
        action.accept(connection);
        ConnectionPool.close(connection);
    }

    public void executorS(String sentence) throws Exception {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        statement.execute(sentence);
        ConnectionPool.close(connection, statement);
    }

}


