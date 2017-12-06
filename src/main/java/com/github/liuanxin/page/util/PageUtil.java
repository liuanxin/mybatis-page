package com.github.liuanxin.page.util;

import org.apache.ibatis.mapping.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author https://github.com/liuanxin
 */
public class PageUtil {

    private static final List<ResultMapping> EMPTY_RESULT_MAPPINGS = new ArrayList<ResultMapping>(0);

    private static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;
        BoundSqlSqlSource(MappedStatement ms, Object parameter, String sql) {
            BoundSql oldBoundSql = ms.getBoundSql(parameter);
            List<ParameterMapping> parameterMappings = oldBoundSql.getParameterMappings();
            this.boundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);

            // handler 「There is no getter for property named '__frch_criterion_1'」 Exception
            for (ParameterMapping mapping : parameterMappings) {
                String property = mapping.getProperty();
                if (oldBoundSql.hasAdditionalParameter(property)) {
                    this.boundSql.setAdditionalParameter(property, oldBoundSql.getAdditionalParameter(property));
                }
            }
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    public static MappedStatement copyFromNewSql(MappedStatement ms, Object parameter, String sql, boolean countQuery) {
        // ms no set sql method...

        String id = ms.getId();
        // count query has custom id
        if (countQuery) {
            id += "_COUNT";
        }
        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(), id, new BoundSqlSqlSource(ms, parameter, sql), ms.getSqlCommandType()
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

        // count query return int
        if (countQuery) {
            ResultMap.Builder mapBuilder = new ResultMap.Builder(ms.getConfiguration(), id, Integer.class, EMPTY_RESULT_MAPPINGS);
            builder.resultMaps(Collections.singletonList(mapBuilder.build()));
        } else {
            builder.resultMaps(ms.getResultMaps());
        }
        builder.resultSetType(ms.getResultSetType());

        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }
}
