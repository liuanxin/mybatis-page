package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;

/**
 * @author https://github.com/liuanxin
 */
public class MySqlDialect extends Dialect {

    public MySqlDialect(String sql, PageBounds pageBounds) {
        super(sql, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        StringBuilder sbd = new StringBuilder(15 + sql.length());
        sbd.append(sql);
        if (offset > 0) {
            sbd.append(" LIMIT ?, ?");
            super.addPageParam(offsetName, offset);
            super.addPageParam(limitName, limit);
        } else {
            sbd.append(" LIMIT ?");
            super.addPageParam(limitName, limit);
        }
        return sbd.toString();
    }
}
