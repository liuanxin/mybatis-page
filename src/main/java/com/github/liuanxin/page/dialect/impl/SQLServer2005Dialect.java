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
    protected String getLimitString(String sql, int offset, int limit) {
        if (offset > 0) {
            return SqlServerUtil.rowNum(sql, offset, limit);
        } else {
            return SqlServerUtil.topPage(sql, limit);
        }
    }
}
