package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;

/**
 * @author https://github.com/liuanxin
 */
public class SQLServer2012Dialect extends Dialect {

    public SQLServer2012Dialect(String sql, PageBounds pageBounds) {
        super(sql, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, int offset, int limit) {
        if (offset > 0) {
            return SqlServerUtil.fetchNext(sql, offset, limit);
        } else {
            return SqlServerUtil.topPage(sql, limit);
        }
    }
}
