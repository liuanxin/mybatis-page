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
    protected String getLimitString(String sql, int offset, int limit) {
        StringBuilder sbd = new StringBuilder(20 + sql.length());
        sbd.append(sql);
        if (offset > 0) {
            sbd.append(" LIMIT ").append(offset).append(", ").append(limit);
        } else {
            sbd.append(" LIMIT ").append(limit);
        }
        return sbd.toString();
    }
}
