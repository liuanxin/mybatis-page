package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.Page;
import org.apache.ibatis.mapping.MappedStatement;

public class OracleDialect extends Dialect {

    public OracleDialect(MappedStatement mappedStatement, Object parameterObject, Page page) {
        super(mappedStatement, parameterObject, page);
    }

    @Override
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        sql = sql.trim();
        boolean isForUpdate = false;
        if (sql.toUpperCase().endsWith(FOR_UPDATE)) {
            sql = sql.substring(0, sql.length() - FOR_UPDATE.length());
            isForUpdate = true;
        }

        StringBuilder sbd = new StringBuilder(sql.length());
        if (offset > 0) {
            sbd.append("SELECT * FROM ( SELECT _ROW_.*, ROWNUM _ROW_NUM_ FROM ( ");
        } else {
            sbd.append("SELECT * FROM ( ");
        }
        sbd.append(sql);
        if (offset > 0) {
            sbd.append(" ) _ROW_ ) WHERE _ROW_NUM_ <= ? AND _ROW_NUM_ > ?");
            setPageParameter("__offsetEnd", offset + limit);
            setPageParameter(offsetName, offset);
        } else {
            sbd.append(" ) WHERE ROWNUM <= ?");
            setPageParameter(limitName, limit);
        }

        if (isForUpdate) {
            sbd.append(FOR_UPDATE);
        }
        return sbd.toString();
    }
}

