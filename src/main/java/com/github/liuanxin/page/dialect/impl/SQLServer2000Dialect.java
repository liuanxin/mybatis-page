package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author https://github.com/liuanxin
 */
public class SQLServer2000Dialect extends Dialect {

    public SQLServer2000Dialect(MappedStatement mappedStatement, Object parameterObject, PageBounds pageBounds) {
        super(mappedStatement, parameterObject, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, int offset, int limit) {
        return SqlServerUtil.topPage(sql, limit);
    }
}
