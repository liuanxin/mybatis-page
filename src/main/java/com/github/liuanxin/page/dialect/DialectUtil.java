package com.github.liuanxin.page.dialect;

import com.github.liuanxin.page.dialect.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author https://github.com/liuanxin
 */
public class DialectUtil {

    private static final Map<String, Class<? extends Dialect>> DIALECT_MAP = new HashMap<String, Class<? extends Dialect>>();
    static {
        DIALECT_MAP.put("mysql", MySqlDialect.class);
        DIALECT_MAP.put("oracle", OracleDialect.class);
        DIALECT_MAP.put("postgresql", PostgreSQLDialect.class);

        DIALECT_MAP.put("sqlite", MySqlDialect.class);
        DIALECT_MAP.put("h2", H2Dialect.class);

        DIALECT_MAP.put("sqlserver2000", SQLServer2000Dialect.class);
        DIALECT_MAP.put("sqlserver2005", SQLServer2005Dialect.class);
        DIALECT_MAP.put("sqlserver2012", SQLServer2012Dialect.class);
    }

    public static Class<? extends Dialect> getDialect(String dialect) {
        return DIALECT_MAP.get(dialect.toLowerCase());
    }
}
