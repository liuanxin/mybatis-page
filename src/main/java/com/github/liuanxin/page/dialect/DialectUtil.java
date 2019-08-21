package com.github.liuanxin.page.dialect;

import com.github.liuanxin.page.dialect.impl.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author https://github.com/liuanxin
 */
public class DialectUtil {

    private static final Map<String, Class<? extends Dialect>> DIALECT_MAP = new HashMap<String, Class<? extends Dialect>>();
    static {
        DIALECT_MAP.put("h2", H2Dialect.class);

        DIALECT_MAP.put("postgresql", PostgreSQLDialect.class);
        DIALECT_MAP.put("oracle", OracleDialect.class);

        DIALECT_MAP.put("sqlite", MySqlDialect.class);
        DIALECT_MAP.put("mysql", MySqlDialect.class);
        DIALECT_MAP.put("mariadb", MySqlDialect.class);


        DIALECT_MAP.put("microsoft", SQLServer2000Dialect.class); // for sql server 2000 jdbc
        DIALECT_MAP.put("sqlserver", SQLServer2005Dialect.class); // for sql server 2005/2008 jdbc

        DIALECT_MAP.put("sqlserver2000", SQLServer2000Dialect.class);

        DIALECT_MAP.put("sqlserver2005", SQLServer2005Dialect.class);
        DIALECT_MAP.put("sqlserver2008", SQLServer2005Dialect.class);
        // same with sql server 2012 and above
        DIALECT_MAP.put("sqlserver2012", SQLServer2012Dialect.class);
        DIALECT_MAP.put("sqlserver2014", SQLServer2012Dialect.class);
        DIALECT_MAP.put("sqlserver2016", SQLServer2012Dialect.class);
        DIALECT_MAP.put("sqlserver2017", SQLServer2012Dialect.class);
    }

    public static Class<? extends Dialect> getDialect(String dialect) {
        return DIALECT_MAP.get(dialect.toLowerCase());
    }

    public static Class<? extends Dialect> getDbType(DataSource dataSource) {
        if (dataSource == null) {
            return null;
        }
        Connection con = null;
        try {
            con = dataSource.getConnection();
            if (con == null) {
                return null;
            }
            String url = con.getMetaData().getURL();
            if (url == null || "".equals(url)) {
                return null;
            }
            for (Map.Entry<String, Class<? extends Dialect>> entry : DIALECT_MAP.entrySet()) {
                // sql server 2005 same to 2012, so if use sql server 2012, must manual setting
                // if use <log4jdbc> framework to record complete sql
                if (url.startsWith("jdbc:" + entry.getKey() + ":")
                        || url.startsWith("jdbc:log4jdbc:" + entry.getKey() + ":")) {
                    return entry.getValue();
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load db type with datasource.", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }
}
