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
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        if (offset > 0) {
            return fetchNext(sql, offsetName, offset, limitName, limit);
        } else {
            super.addPageParam(limitName, limit);
            return SqlServerUtil.topPage(sql);
        }
    }

    /** 只在 2012 及以上的版本开始才有效 */
    private String fetchNext(String sql, String offsetName, int offset, String limitName, int limit) {
        StringBuilder sbd = new StringBuilder(50 + sql.length());
        sbd.append(sql).append(" ");
        if (!sql.toUpperCase().contains(SqlServerUtil.ORDER_BY)) {
            sbd.append(SqlServerUtil.DEFAULT_ORDER_BY);
        }
        sbd.append(" OFFSET ? ROWS");
        super.addPageParam(offsetName, offset);
        if (limit > 0) {
            sbd.append(" FETCH NEXT ? ROWS ONLY");
            super.addPageParam(limitName, limit);
        }
        return sbd.toString();
    }
}
