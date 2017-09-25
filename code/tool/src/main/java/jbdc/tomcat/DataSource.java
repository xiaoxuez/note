package jbdc.tomcat;

/**
 * Created by xiaoxuez on 2017/9/7.
 */
public class DataSource {

    private String name;
    private String driver = "com.mysql.jdbc.Driver";
    private String jdbcUrl;
    private String user;
    private String pwd;
    private int maxConn = 20;
    private int initConn = 10;

    public String getName() {
        return name;
    }

    public DataSource setName(String name) {
        this.name = name;
        return this;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public DataSource setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DataSource setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPwd() {
        return pwd;
    }

    public DataSource setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    public int getMaxConn() {
        return maxConn;
    }

    public DataSource setMaxConn(int maxConn) {
        this.maxConn = maxConn;
        return this;
    }

    public int getInitConn() {
        return initConn;
    }

    public DataSource setInitConn(int initConn) {
        this.initConn = initConn;
        return this;
    }




}
