package com.github.liuanxin.page.dialect;

import com.github.liuanxin.page.model.PageBounds;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialect {

    protected static final String FOR_UPDATE = " FOR UPDATE";

    protected MappedStatement ms;
    protected PageBounds page;
    protected Object params;
    protected BoundSql boundSql;
    protected List<ParameterMapping> parameterMappings;
    protected Map<String, Object> pageParameters = new HashMap<String, Object>();
    protected String sql;

    public Dialect(MappedStatement mappedStatement, Object params, PageBounds page) {
        this.ms = mappedStatement;
        this.params = params;
        this.page = page;

        init();
    }

    @SuppressWarnings("unchecked")
    protected void init() {
        boundSql = ms.getBoundSql(params);
        parameterMappings = new ArrayList<ParameterMapping>(boundSql.getParameterMappings());
        if (params instanceof Map) {
            pageParameters.putAll((Map) params);
        } else {
            for (ParameterMapping parameterMapping : parameterMappings) {
                pageParameters.put(parameterMapping.getProperty(), params);
            }
        }

        StringBuilder sbd = new StringBuilder(boundSql.getSql().trim());
        if (sbd.lastIndexOf(";") == sbd.length() - 1) {
            sbd.deleteCharAt(sbd.length() - 1);
        }
        this.sql = sbd.toString();
    }

    protected void setPageParameter(String name, Object value) {
        parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), name, Integer.class).build());
        pageParameters.put(name, value);
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
            return getLimitString(sql, "__offset", page.getOffset(), "__limit", page.getLimit());
        }
    }
    public String getCountSQL() {
        return "SELECT COUNT(1) FROM (" + sql + ") TMP_COUNT";
    }

    protected String getLimitString(String sql, String offsetName, int offset, String limitName, int limit) {
        throw new UnsupportedOperationException("Must set Dialect! Just like MySql Oracle etc.");
    }
}
