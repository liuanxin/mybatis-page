package com.github.liuanxin.page.dialect.impl;

/**
 * @author https://github.com/liuanxin
 */
class SqlServerUtil {

    private static final String SELECT = "SELECT ";
    private static final int SELECT_LEN = SELECT.length();

    private static final String DISTINCT = " DISTINCT ";
    private static final String SELECT_DISTINCT = "SELECT DISTINCT ";
    private static final int DISTINCT_LEN = SELECT_DISTINCT.length();

    static final String ORDER_BY = " ORDER BY ";
    static final String DEFAULT_ORDER_BY = " ORDER BY 1 ";

    /** 主要针对于 sql server 2000 或 只查询头几条数据的 sql 语句, 这是最简单的处理方式(select top 10 f1,f2,f3 from t_xxx ...) */
    static String topPage(String sql) {
        String upperCase = sql.toUpperCase();
        int insertPoint = upperCase.indexOf(SELECT) + (upperCase.startsWith(SELECT_DISTINCT) ? DISTINCT_LEN : SELECT_LEN);

        return new StringBuilder(sql).insert(insertPoint, " TOP ? ").toString();
    }

    /*
    2005 开始使用 row_number() 函数来分页

    with paged as (
      select -- top :offset + :limit
      row_number() over(order by ...) as rn_,
      xxx from xxx       -- 不带 select 和 order by xx 的原始 sql
    ) select * from paged
    where rn_ > :offset and rn_ <= :offset + :limit

    或者下面的语句

    select * from (
      select -- top :offset + :limit
      row_number() over (order by ...) as rn_,
      xxx from xxx       -- 不带 select 和 order by xx 的原始 sql
    ) a_ where a_.rn_ > :offset and a_.rn_ <= :offset + :limit


    单从使用上来说, row_number() 是最为复杂的版本. 下面的方式只支持 sql server 2012 及以上的版本

    select xxx from xxx order by xx   -- 原始 sql
    offset :offset rows
    fetch next :limit rows only
    */

    /** 主要针对 2005 及 2008 支持 row_number 函数的版本 */
    static String rowNum(String sql) {
        StringBuilder sbd = new StringBuilder(50 + sql.length());
        /*
        sbd.append("WITH PAGED AS ( SELECT ");
        if (hasDistinct(sql)) {
            sbd.append(DISTINCT);
        }
        // sbd.append(" TOP ").append(offset + limit);
        sbd.append(" ROW_NUMBER() OVER (").append(orderBy(sql)).append(") AS RN_, ");
        sbd.append(noSelectNoOrderSql(sql));
        sbd.append(" ) SELECT * FROM PAGED");
        sbd.append(" WHERE RN_ > ? AND RN_ <= ?");
        */

        sbd.append("SELECT * FROM ( SELECT ");
        if (hasDistinct(sql)) {
            sbd.append(DISTINCT);
        }
        // sbd.append(" TOP top ").append(offset + limit);
        sbd.append(" ROW_NUMBER() OVER (").append(orderBy(sql)).append(") AS RN_, ");
        sbd.append(noSelectNoOrderSql(sql));
        sbd.append(" ) A_ WHERE RN_ > ? AND RN_ <= ?");
        return sbd.toString();
    }
    private static boolean hasDistinct(String sql) {
        return sql.toUpperCase().contains(SELECT_DISTINCT);
    }
    /** 排序字段, 如果语句中没有使用时间 */
    private static String orderBy(String sql) {
        int orderByIndex = sql.toUpperCase().indexOf(ORDER_BY);
        return orderByIndex > 0 ? sql.substring(orderByIndex) : DEFAULT_ORDER_BY;
    }
    /** 不带 select 和 order by xx 的原始 sql */
    private static String noSelectNoOrderSql(String sql) {
        String upperCase = sql.toUpperCase();
        int start = upperCase.indexOf(SELECT) + (upperCase.contains(DISTINCT) ? DISTINCT_LEN : SELECT_LEN);
        int end = upperCase.indexOf(ORDER_BY);
        if (end <= 0) {
            end = sql.length();
        }
        return sql.substring(start, end);
    }
}
