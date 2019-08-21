package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;

/**
 * @author https://github.com/liuanxin
 */
public class PostgreSQLDialect extends Dialect {

    public PostgreSQLDialect(String sql, PageBounds pageBounds) {
        super(sql, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        StringBuilder sbd = new StringBuilder(20 + sql.length());
        sbd.append(sql).append(" LIMIT ?");
        super.addPageParam(limitName, limit);
        if (offset > 0) {
            sbd.append(" OFFSET ?");
            super.addPageParam(offsetName, offset);
        }
        return sbd.toString();
    }
}
