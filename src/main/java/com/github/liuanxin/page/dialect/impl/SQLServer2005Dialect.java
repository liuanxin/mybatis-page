package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;

/**
 * @author https://github.com/liuanxin
 */
public class SQLServer2005Dialect extends Dialect {

    public SQLServer2005Dialect(String sql, PageBounds pageBounds) {
        super(sql, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        if (offset > 0) {
            super.addPageParam(offsetName, offset);
            super.addPageParam(limitName, offset + limit);
            return SqlServerUtil.rowNum(sql);
        } else {
            super.addPageParam(limitName, limit);
            return SqlServerUtil.topPage(sql);
        }
    }
}
