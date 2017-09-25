package jbdc.tomcat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by xiaoxuez on 2017/9/7.
 */
public class ConnectionUtils {
    private static final Logger logger = LoggerFactory
            .getLogger(ConnectionUtils.class);

    public static void free(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            logger.error("关闭ResultSet出错",e);
        } finally {
            try {
                if (st != null)
                    st.close();
            } catch (SQLException e) {
                logger.error("关闭Statement出错",e);
            } finally {
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    logger.error("关闭Connection出错",e);
                }
            }
        }
    }

    public static void free(ResultSet rs, Statement st) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            logger.error("关闭ResultSet出错",e);
        } finally {
            try {
                if (st != null)
                    st.close();
            } catch (SQLException e) {
                logger.error("关闭Statement出错",e);
            }
        }
    }

    public static void free( Connection conn) {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            logger.error("关闭Connection出错",e);
        }
    }

    public static void free(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            logger.error("关闭ResultSet出错",e);
        }
    }

    public static void free(Statement st) {
        try {
            if (st != null)
                st.close();
        } catch (SQLException e) {
            logger.error("关闭Statement出错",e);
        }
    }

}
