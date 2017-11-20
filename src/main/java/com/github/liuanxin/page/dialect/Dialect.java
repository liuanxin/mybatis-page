package com.github.liuanxin.page.dialect;

import com.github.liuanxin.page.model.PageBounds;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author https://github.com/liuanxin
 */
public class Dialect {

    private static final String FOR_UPDATE = " FOR UPDATE";
    private static final String ORDER_BY = "ORDER BY ";
    /** multi blank char */
    private static final Pattern BLANK_REGEX = Pattern.compile("\\s{2,}");

    private MappedStatement ms;
    protected PageBounds page;
    private Object params;
    private List<ParameterMapping> parameterMappings;
    private Map<String, Object> pageParameters = new HashMap<String, Object>();
    private String sql;

    public Dialect(MappedStatement mappedStatement, Object params, PageBounds page) {
        this.ms = mappedStatement;
        this.params = params;
        this.page = page;

        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        BoundSql boundSql = ms.getBoundSql(params);
        parameterMappings = new ArrayList<ParameterMapping>(boundSql.getParameterMappings());
        if (params instanceof Map) {
            pageParameters.putAll((Map) params);
        } else {
            for (ParameterMapping parameterMapping : parameterMappings) {
                pageParameters.put(parameterMapping.getProperty(), params);
            }
        }

        StringBuilder sbd = new StringBuilder(boundSql.getSql());
        if (sbd.lastIndexOf(";") == sbd.length() - 1) {
            sbd.deleteCharAt(sbd.length() - 1);
        }
        // 多空白符替换成一个, 这里操作一次, 其他地方只需要处理大写就可以了
        this.sql = BLANK_REGEX.matcher(sbd.toString().trim()).replaceAll(" ");
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }
    public Object getParameterObject() {
        return pageParameters;
    }

    public String getPageSQL(Integer count) {
        if (page.notNeedPage()) {
            return sql;
        } else {
            page.pageWrong(count);

            // 如果原始 sql 中带有 for update, 放在分页后的 sql 最后
            String limitSql = sql;
            boolean hasForUpdate = false;
            String upperCase = limitSql.toUpperCase();
            if (upperCase.endsWith(FOR_UPDATE)) {
                limitSql = sql.substring(0, sql.length() - FOR_UPDATE.length());
                hasForUpdate = true;
            }
            limitSql = getLimitString(limitSql, page.getOffset(), page.getLimit());
            if (hasForUpdate) {
                limitSql = limitSql + FOR_UPDATE;
            }
            return limitSql;
        }
    }

    public String getCountSQL() {
        String countSql = sql;
        String upperCase = countSql.toUpperCase();
        // count sql query don't need <for update>
        if (upperCase.endsWith(FOR_UPDATE)) {
            countSql = countSql.substring(0, countSql.length() - FOR_UPDATE.length());
        }
        // count sql query don't need <order by xxx>
        if (upperCase.contains(ORDER_BY)) {
            countSql = countSql.substring(0, upperCase.indexOf(ORDER_BY));
        }
        return "SELECT COUNT(1) FROM (" + countSql + ") TEMP_COUNT";
    }

    protected String getLimitString(String sql, int offset, int limit) {
        throw new UnsupportedOperationException("Must set Dialect! Just like MySql Oracle etc.");
    }
}
