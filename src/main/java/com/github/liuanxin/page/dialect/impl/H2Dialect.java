package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;

/**
 * SQLite 可以用此种方式, 也可以用 MySQL 的方式
 *
 * @author https://github.com/liuanxin
 */
public class H2Dialect extends Dialect {

    public H2Dialect(String sql, PageBounds pageBounds) {
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
