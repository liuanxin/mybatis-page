package com.github.mte.page.util;

import com.github.mte.page.model.Page;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PageUtil {

    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(CPU_NUM);

    public static <T> T submit(Callable<T> callable) throws ExecutionException, InterruptedException {
        return POOL.submit(callable).get();
    }
    public static void clear() {
        POOL.shutdown();
    }

    public static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    public static MappedStatement copyFromNewSql(MappedStatement ms, BoundSql boundSql, String sql,
                                                 List<ParameterMapping> parameterMappings, Object parameter) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(),
                ms.getId(),
                new BoundSqlSqlSource(copyFromBoundSql(ms, boundSql, sql, parameterMappings, parameter)),
                ms.getSqlCommandType()
        );

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }

        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());

        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());

        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    public static BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql,
                                            String sql, List<ParameterMapping> parameterMappings, Object parameter) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    public static int getCount(Executor executor, MappedStatement ms,
                               Object param, Page page, String sql) throws SQLException {
        Integer count;
        Cache cache = ms.getCache();
        if (cache != null && ms.isUseCache() && ms.getConfiguration().isCacheEnabled()) {
            BoundSql boundSql = ms.getBoundSql(param);
            BoundSql newBoundSql = copyFromBoundSql(ms, boundSql, sql,
                    boundSql.getParameterMappings(), boundSql.getParameterObject());

            CacheKey cacheKey = executor.createCacheKey(ms, param, page, newBoundSql);
            count = (Integer) cache.getObject(cacheKey);
            if (count == null) {
                count = queryCount(ms, param, sql);
                cache.putObject(cacheKey, count);
            }
        } else {
            count = queryCount(ms, param, sql);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static void setParameters(final MappedStatement mappedStatement,
                                      final Object parameterObject,
                                      PreparedStatement ps) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());

        final BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException(String.format("no TypeHandler found for parameter %s of statement %s",
                                propertyName, mappedStatement.getId()));
                    }
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) jdbcType = configuration.getJdbcTypeForNull();
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                }
            }
        }
    }
    private static int queryCount(MappedStatement ms, Object param, String sql) throws SQLException {
        Connection con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = ms.getConfiguration().getEnvironment().getDataSource().getConnection();
            st = con.prepareStatement(sql);
            setParameters(ms, param, st);

            rs = st.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (st != null) {
                        st.close();
                    }
                } finally {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                }
            }
        }
    }
}
