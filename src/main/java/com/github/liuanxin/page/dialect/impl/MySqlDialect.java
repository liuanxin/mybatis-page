package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.Page;
import org.apache.ibatis.mapping.MappedStatement;

public class MySqlDialect extends Dialect {

    public MySqlDialect(MappedStatement mappedStatement, Object parameterObject, Page page) {
        super(mappedStatement, parameterObject, page);
    }

    @Override
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        sql = sql.trim();
        boolean hasForUpdate = false;
        if (sql.toLowerCase().endsWith(FOR_UPDATE)) {
            sql = sql.substring(0, sql.length() - FOR_UPDATE.length());
            hasForUpdate = true;
        }

        StringBuilder sbd = new StringBuilder(sql);
        if (offset > 0) {
            sbd.append(" LIMIT ?, ?");
            setPageParameter(offsetName, offset);
            setPageParameter(limitName, limit);
        } else {
            sbd.append(" LIMIT ?");
            setPageParameter(limitName, limit);
        }

        if (hasForUpdate) {
            sbd.append(FOR_UPDATE);
        }
        return sbd.toString();
    }
}
