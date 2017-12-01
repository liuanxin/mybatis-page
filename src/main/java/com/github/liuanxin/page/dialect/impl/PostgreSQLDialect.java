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
    protected String getLimitString(String sql, int offset, int limit) {
        StringBuilder sbd = new StringBuilder(20 + sql.length());
        sbd.append(sql).append(" LIMIT ").append(limit);
        if (offset > 0) {
            sbd.append(" OFFSET ").append(offset);
        }
        return sbd.toString();
    }
}
