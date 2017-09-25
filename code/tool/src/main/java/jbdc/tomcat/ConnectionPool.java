package jbdc.tomcat;


import org.apache.tomcat.dbcp.dbcp.ConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolingDriver;
import org.apache.tomcat.dbcp.pool.ObjectPool;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import java.sql.*;
import java.util.Set;

/**
 * Created by xiaoxuez on 2017/9/7.
 *
 * 这里我采用了tomcat的封装， tomcat-dbcp = commons-dbcp2 + commons-pool,  example可以看到commons中的使用，这个写成个通用的吧就用tomcat-dbcp
 * 使用的时候加一个依赖就可以了，其实二者在dbcp上没什么区别。
 */
public class ConnectionPool {
    public static PoolingDriver pd = new PoolingDriver();

    private static Set<String> dsNameSet = new java.util.concurrent.ConcurrentSkipListSet<String>();

    private ConnectionPool() {

    }

    public static synchronized void createConnectionPool(DataSource ds) {
        if (!dsNameSet.contains(ds.getName())) {
            initDbPool(ds);
            dsNameSet.add(ds.getName());
        }
    }

    /**
     * 初始换连接池
     *
     * @throws Exception
     */
    private static void initDbPool(DataSource ds) {
        try {
            Class.forName(ds.getDriver());
            GenericObjectPool pool = new GenericObjectPool(null);
            pool.setMaxActive(ds.getMaxConn());
            pool.setMaxWait(50000);
            pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
            ConnectionFactory connFactory = new DriverManagerConnectionFactory(ds.getJdbcUrl(),ds.getUser(),ds.getPwd());
            new PoolableConnectionFactory(connFactory, pool, null, null, false, true);
            for (int i = 0; i < ds.getInitConn(); i++) {
                pool.addObject();
            }
            pd.registerPool(ds.getName(), pool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从链接池中获取链接
     *
     * @param dsname
     * @return
     * @throws SQLException
     */
    public static synchronized Connection getConnection(String dsname) throws Exception {
        try{
            Connection conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + dsname);
            return conn;
        }catch(Throwable t){
            t.printStackTrace();
            throw new Exception(t);
        }
    }

    public static synchronized ObjectPool getConnectionPool(String dsname) throws Exception {
        try{
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:" + dsname);
            return driver.getConnectionPool(dsname);
        }catch(Throwable t){
            t.printStackTrace();
            throw new Exception(t);
        }
    }


    public static synchronized Connection getConnection(DataSource ds) throws Exception {
        try{
            createConnectionPool(ds);
            Connection conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + ds.getName());
            conn.setAutoCommit(false);
            return conn;
        }catch(Throwable t){
            throw new Exception(t);
        }
    }

    /**
     * 关闭连接池
     *
     * @throws SQLException
     */
    public static synchronized void shutdownPool() {
        try{
            for (String key : dsNameSet) {
                PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:"+key);
                driver.closePool(key);
            }
            dsNameSet.clear();
        }catch(Throwable t){

        }

    }

    /**
     * 关闭连接池
     *
     * @throws SQLException
     */
    public static synchronized void shutdownPool(String dsName) throws Exception {
        try{
            if (dsNameSet.contains(dsName)) {
                PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:"+dsName);
                driver.closePool(dsName);
            }
            dsNameSet.remove(dsName);
        }catch(Throwable t){
            throw new Exception(t);
        }

    }
    /**
     *  关闭连接
     * @param conn
     * @throws SQLException
     */
    public static void close(Connection conn){
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭Statement
     * @param st
     * @throws SQLException
     */
    public static void close(Statement st) {
        if (st == null) {
            return;
        }
        try {
            if(!st.isClosed())
                st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭结果集
     * @param rs
     * @throws SQLException
     */
    public static void close(ResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection conn, Statement st, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (st != null)
                st.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection conn, Statement st) {
        try {
            if (st != null)
                st.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
