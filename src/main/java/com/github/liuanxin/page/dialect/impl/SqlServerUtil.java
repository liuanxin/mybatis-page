package com.github.liuanxin.page.dialect.impl;

/**
 * @author https://github.com/liuanxin
 */
class SqlServerUtil {

    private static final String SELECT = "SELECT ";
    private static final int SELECT_LEN = SELECT.length();

    private static final String DISTINCT = " DISTINCT ";
    private static final String SELECT_DISTINCT = (SELECT + DISTINCT).replace("  ", " ");
    private static final int DISTINCT_LEN = SELECT_DISTINCT.length();

    private static final String ORDER_BY = "ORDER BY ";

    /** 主要针对于 sql server 2000 或 只查询头几条数据的 sql 语句, 这是最简单的处理方式(select top xxx) */
    static String topPage(String sql, int limit) {
        String upperCase = sql.toUpperCase();
        int insertPoint = upperCase.indexOf(SELECT) + (upperCase.contains(DISTINCT) ? DISTINCT_LEN : SELECT_LEN);

        return new StringBuilder(10 + sql.length())
                .append(sql)
                .insert(insertPoint, String.format(" TOP %d ", limit)).toString();
    }

    /*
    with paged as (
      select top :offset + :limit row_number() over(order by ...) as rn_,
      xxx from xxx       -- 不带 select 和 order by xx 的原始 sql
    ) select * from paged
    where rn_ > :offset and rn_ <= :offset + :limit

    或者下面的语句

    select * from (
      select top :offset + :limit row_number() over (order by ...) as rn_,
      xxx from xxx       -- 不带 select 和 order by xx 的原始 sql
    ) a_ where rn_ > :offset and rn_ <= :offset + :limit


    下面的方式只支持 sql server 2012

    select xxx from xxx order by xx   -- 原始 sql
    offset :offset rows
    fetch next :limit rows only
    */

    /** 只在 2012 的版本开始才有效 */
    static String fetchNext(String sql, int offset, int limit) {
        StringBuilder sb = new StringBuilder(50 + sql.length());
        sb.append(sql);
        if (!sql.toUpperCase().contains(ORDER_BY)) {
            sb.append(ORDER_BY + " 1 ");
        }
        sb.append(" OFFSET ").append(offset).append(" ROWS");
        if (limit > 0) {
            sb.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY");
        }
        return sb.toString();
    }

    /** 主要针对 2005 及 2008 支持 row_number 函数的版本 */
    static String rowNum(String sql, int offset, int limit) {
        StringBuilder sbd = new StringBuilder(50 + sql.length());
        /*
        sbd.append("with paged as ( select ");
        if (hasDistinct(sql)) {
            sbd.append(DISTINCT);
        }
        /* sbd.append(" top ").append(offset + limit); * /
        sbd.append(" row_number() over (").append(orderBy(sql)).append(" rn_, ").append(noSelectNoOrderSql(sql));
        sbd.append(" ) select * from paged");
        sbd.append(" where rn_ > ").append(offset).append(" and rn_ <= ").append(offset + limit);
        */

        sbd.append("select * from ( select ");
        if (hasDistinct(sql)) {
            sbd.append(DISTINCT);
        }
        /* sbd.append(" top ").append(offset + limit); */
        sbd.append(" row_number() over (").append(orderBy(sql)).append(" rn_, ").append(noSelectNoOrderSql(sql));
        sbd.append(" ) a_ where rn_ > ").append(offset).append(" and rn_ <= ").append(offset + limit);

        return sbd.toString();
    }
    private static boolean hasDistinct(String sql) {
        return sql.contains(SELECT_DISTINCT);
    }
    /** 排序字段, 如果语句中没有使用时间 */
    private static String orderBy(String sql) {
        int orderByIndex = sql.toUpperCase().indexOf(ORDER_BY);
        return orderByIndex > 0 ? sql.substring(orderByIndex) : ORDER_BY + " CURRENT_TIMESTAMP";
    }
    /** 不带 select 和 order by xx 的原始 sql */
    private static String noSelectNoOrderSql(String sql) {
        String upperCase = sql.toUpperCase();
        int start = upperCase.indexOf(SELECT) + SELECT_LEN;
        int end = upperCase.indexOf(ORDER_BY);
        if (end <= 0) {
            end = sql.length();
        }
        return sql.substring(start, end);
    }
}
