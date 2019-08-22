package com.github.liuanxin.page.dialect;

import com.github.liuanxin.page.model.PageBounds;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author https://github.com/liuanxin
 */
public class Dialect {

    private static final String FOR_UPDATE = " FOR UPDATE";
    private static final String GROUP_BY = " GROUP BY ";

    private static final Pattern COUNT_REGEX = Pattern.compile("^SELECT (.*?) FROM ");
    // COUNT (*) is the sql specification, it's not slower than COUNT (NUM)
    private static final String COUNT = "SELECT COUNT(*) FROM ";

    /** multi blank char */
    private static final Pattern BLANK_REGEX = Pattern.compile("\\s{2,}");
    private static final String BLANK = " ";

    protected PageBounds page;
    private String sql;
    private Map<String, Object> pageParams;

    public Dialect(String sql, PageBounds page) {
        this.page = page;

        StringBuilder sbd = new StringBuilder(sql);
        if (sbd.lastIndexOf(";") == sbd.length() - 1) {
            sbd.deleteCharAt(sbd.length() - 1);
        }
        // multi blank replace to one blank
        this.sql = BLANK_REGEX.matcher(sbd.toString()).replaceAll(BLANK).trim();
        this.pageParams = new LinkedHashMap<>();
    }

    protected void addPageParam(String name, Object value) {
        pageParams.put(name, value);
    }

    public Map<String, Object> getPageParams() {
        return pageParams;
    }

    public String getPageSQL(Integer count) {
        if (page.notNeedPage()) {
            return sql;
        } else {
            page.pageWrong(count);

            // if original sql have <for update>, put in sql end
            String limitSql = sql;
            boolean hasForUpdate = false;
            String upperCase = limitSql.toUpperCase();
            if (upperCase.endsWith(FOR_UPDATE)) {
                limitSql = sql.substring(0, sql.length() - FOR_UPDATE.length());
                hasForUpdate = true;
            }

            limitSql = getLimitString(limitSql, "_offset_", page.getOffset(), "_limit_", page.getLimit());

            if (hasForUpdate) {
                limitSql = limitSql + FOR_UPDATE;
            }
            return limitSql.trim();
        }
    }

    public String getCountSQL() {
        String countSql = sql;

        String upperCase = countSql.toUpperCase();
        // count sql query don't need <for update>
        if (upperCase.endsWith(FOR_UPDATE)) {
            countSql = countSql.substring(0, countSql.length() - FOR_UPDATE.length());
        }

        if (upperCase.contains(GROUP_BY)) {
            return COUNT + " (" + countSql + ") TEMP_COUNT";
        } else {
            // count sql query don't need <order by xxx>
            /*String orderBy = " ORDER BY ";
            if (upperCase.contains(orderBy)) {
                countSql = countSql.substring(0, upperCase.indexOf(orderBy));
            }*/
            return COUNT_REGEX.matcher(countSql).replaceFirst(COUNT);
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        throw new UnsupportedOperationException("Must set Dialect! Just like MySql Oracle etc.");
    }
}
