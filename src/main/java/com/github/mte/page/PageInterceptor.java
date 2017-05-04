package com.github.mte.page;

import com.github.mte.page.dialect.Dialect;
import com.github.mte.page.dialect.impl.MySqlDialect;
import com.github.mte.page.dialect.impl.OracleDialect;
import com.github.mte.page.model.Page;
import com.github.mte.page.model.PageList;
import com.github.mte.page.util.PageUtil;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}
 */
@Intercepts({
        @Signature(
                type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
public class PageInterceptor implements Interceptor {

    private static final Map<String, Class<? extends Dialect>> DIALECT_MAP = new HashMap<String, Class<? extends Dialect>>();
    static {
        DIALECT_MAP.put("mysql", MySqlDialect.class);
        DIALECT_MAP.put("oracle", OracleDialect.class);
    }

    private static final int MAPPED_INDEX = 0;
    private static final int PARAM_INDEX = 1;
    private static final int ROW_INDEX = 2;


    private Class<? extends Dialect> dialect;

    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(final Invocation invocation) throws Throwable {
        if (dialect == null) {
            return invocation.proceed();
        }

        Object[] args = invocation.getArgs();
        Object rowBounds = args[ROW_INDEX];
        if (!(rowBounds instanceof Page)) {
            return invocation.proceed();
        }
        final Page page = (Page) rowBounds;
        if (page.notNeedPage()) {
            return invocation.proceed();
        }

        final MappedStatement ms = (MappedStatement) args[MAPPED_INDEX];
        final Object param = args[PARAM_INDEX];
        final Dialect dialectInstance;
        try {
            Constructor constructor = dialect.getConstructor(MappedStatement.class, Object.class, Page.class);
            Object[] constructor_params = new Object[] { ms, param, page };
            dialectInstance = (Dialect) constructor.newInstance(constructor_params);
        } catch (Exception e) {
            throw new RuntimeException("Cann't instance dialect instance: " + dialect, e);
        }

        final BoundSql boundSql = ms.getBoundSql(param);

        Integer count = null;
        if (page.isQueryTotal()) {
            count = PageUtil.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Integer count;
                    Cache cache = ms.getCache();
                    String countSQL = dialectInstance.getCountSQL();
                    if (cache != null && ms.isUseCache() && ms.getConfiguration().isCacheEnabled()) {
                        Executor executor = (Executor) invocation.getTarget();

                        List<ParameterMapping> mappings = boundSql.getParameterMappings();
                        Object parameterObject = boundSql.getParameterObject();
                        BoundSql sql = PageUtil.copyFromBoundSql(ms, boundSql, countSQL, mappings, parameterObject);
                        CacheKey cacheKey = executor.createCacheKey(ms, param, page, sql);
                        count = (Integer) cache.getObject(cacheKey);
                        if (count == null) {
                            count = PageUtil.getCount(ms, param, countSQL);
                            cache.putObject(cacheKey, count);
                        }
                    } else {
                        count = PageUtil.getCount(ms, param, countSQL);
                    }
                    return count;
                }
            });
        }

        List<ParameterMapping> mappings = dialectInstance.getParameterMappings();
        Object parameterObject = dialectInstance.getParameterObject();
        String pageSQL = dialectInstance.getPageSQL(count);

        args[MAPPED_INDEX] = PageUtil.copyFromNewSql(ms, boundSql, pageSQL, mappings, parameterObject);
        args[PARAM_INDEX] = parameterObject;
        args[ROW_INDEX] = RowBounds.DEFAULT;

        List list = PageUtil.submit(new Callable<List>() {
            public List call() throws Exception {
                return (List) invocation.proceed();
            }
        });
        return (count != null && count >= 0) ? new PageList(list, count) : list;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }


    public PageInterceptor setDialect(String dialect) {
        if (dialect == null || "".equals(dialect.trim())) {
            throw new RuntimeException("must set dialect");
        }
        Class<? extends Dialect> clazz = DIALECT_MAP.get(dialect.toLowerCase());
        if (clazz == null) {
            throw new RuntimeException("no support db dialect with " + dialect);
        }
        
        this.dialect = clazz;
        return this;
    }
}
