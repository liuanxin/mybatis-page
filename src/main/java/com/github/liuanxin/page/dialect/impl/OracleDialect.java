package com.github.liuanxin.page.dialect.impl;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.model.PageBounds;

/**
 * @author https://github.com/liuanxin
 */
public class OracleDialect extends Dialect {

    public OracleDialect(String sql, PageBounds pageBounds) {
        super(sql, pageBounds);
    }

    /*
    select * from (
      select rownum rn_, a_.* from (
        select x from xxx order by xx   -- 原始 sql
      ) a_ where rownum <= :max         -- 直接使用时不能用在 > 上
    ) where rn_ >= :min                 -- 外层的要用别名才可以用 >

    或者下面的三层嵌套

    select * from (
      select rownum rn_, a_.* from (
        select x from xxx order by xx   -- 原始 sql
      ) a_
    ) where rn_ <= :max and rn_ > :min  -- 用别名在最外层

    针对第一页的数据可以简单一点

    select * from (
      select xxx from xxx       -- 原始 sql
    ) where rownum <= :max      -- 直接使用时不能用在 > 上
    */

    @Override
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        StringBuilder sbd = new StringBuilder(60 + sql.length()).append(sql.length());
        if (offset > 0) {
            sbd.append("SELECT * FROM (");
            sbd.append("SELECT A_.*, ROWNUM RN_ FROM (").append(sql).append(") A_ WHERE ROWNUM <= ?");
            sbd.append(") WHERE RN_ > ?");
            super.addPageParam(limitName, offset + limit);
            super.addPageParam(offsetName, offset);
            /*
            sbd.append("SELECT * FROM (");
            sbd.append("SELECT A_.*, ROWNUM RN_ FROM (").append(sql).append(") A_ ");
            sbd.append(") WHERE RN_ <= ? AND RN_ > ?");
            super.addPageParam(limitName, offset + limit);
            super.addPageParam(offsetName, offset);
            */
        } else {
            sbd.append("SELECT * FROM (").append(sql).append(") WHERE ROWNUM <= ?");
            super.addPageParam(limitName, limit);
        }
        return sbd.toString();
    }
}

