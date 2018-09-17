package com.github.liuanxin.page;

import com.github.liuanxin.page.dialect.Dialect;
import com.github.liuanxin.page.dialect.DialectUtil;
import com.github.liuanxin.page.model.PageBounds;
import com.github.liuanxin.page.model.PageList;
import com.github.liuanxin.page.util.PageUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}
 *
 * @author https://github.com/liuanxin
 */
@Intercepts({
    @Signature(
        type = Executor.class, method = "query",
        args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }
    ),
    @Signature(
        type = Executor.class, method = "query",
        args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }
    )
})
public class PageInterceptor implements Interceptor {

    private static final int MAPPED_INDEX = 0;
    private static final int PARAM_INDEX = 1;
    private static final int ROW_INDEX = 2;

    private Class<? extends Dialect> dialect;

    public PageInterceptor() {}
    public PageInterceptor(String dialect) {
        setDialect(dialect);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(final Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Object rowBounds = args[ROW_INDEX];
        if (!(rowBounds instanceof PageBounds)) {
            return invocation.proceed();
        }
        final PageBounds page = (PageBounds) rowBounds;
        if (page.notNeedPage()) {
            return invocation.proceed();
        }

        final MappedStatement ms = (MappedStatement) args[MAPPED_INDEX];
        if (dialect == null) {
            loadDialectWithDataSource(ms);
        }

        final Object param = args[PARAM_INDEX];
        final Dialect dialectInstance;
        try {
            Constructor constructor = dialect.getConstructor(String.class, PageBounds.class);
            dialectInstance = (Dialect) constructor.newInstance(ms.getBoundSql(param).getSql(), page);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instance dialect instance: " + dialect, e);
        }
        // Just <sql> diff, RowBounds use default.
        args[ROW_INDEX] = RowBounds.DEFAULT;

        Integer count = null;
        if (page.isQueryTotal()) {
            String countSQL = dialectInstance.getCountSQL();
            args[MAPPED_INDEX] = PageUtil.copyFromNewSql(ms, param, countSQL, true);
            // handler count query
            List countObj = (List) invocation.proceed();

            if (countObj != null && countObj.size() > 0) {
                count = (Integer) countObj.get(0);

                // if count was 0, don't need to query page data
                if (count != null && count == 0) {
                    return new PageList(Collections.emptyList(), 0);
                }
            }
        }

        String pageSQL = dialectInstance.getPageSQL(count);
        args[MAPPED_INDEX] = PageUtil.copyFromNewSql(ms, param, pageSQL, false);
        // handler page query
        List list = (List) invocation.proceed();
        if (count == null) {
            return list;
        }

        if (list == null || list.isEmpty()) {
            return new PageList(Collections.emptyList(), count);
        } else {
            return new PageList(list, count);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String dialect = properties.getProperty("dialect");
        if (dialect != null && !"".equals(dialect)) {
            setDialect(dialect);
        }
    }

    public PageInterceptor setDialect(String dialect) {
        Class<? extends Dialect> clazz = DialectUtil.getDialect(dialect);
        if (clazz == null) {
            throw new RuntimeException("no support db dialect with " + dialect);
        }
        this.dialect = clazz;
        return this;
    }

    private void loadDialectWithDataSource(MappedStatement ms) {
        // read from connection by data source
        dialect = DialectUtil.getDbType(ms.getConfiguration().getEnvironment().getDataSource());
        if (dialect == null) {
            throw new RuntimeException("must have dialect info");
        }
    }
}
