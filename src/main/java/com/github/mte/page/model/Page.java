package com.github.mte.page.model;

import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;

public class Page extends RowBounds implements Serializable {

    private int page = 1;
    private int limit = 15;
    private boolean queryTotal = false;

    public Page() {}
    public Page(int limit) {
        this.limit = limit;
    }
    public Page(int page, int limit) {
        this.page = page;
        this.limit = limit;
        queryTotal = true;
    }


    public boolean notNeedPage() {
        return getOffset() == RowBounds.NO_ROW_OFFSET && limit == RowBounds.NO_ROW_LIMIT;
    }

    /** If count = 10, limit = 15, But incoming param page value was 2, It's wrong, Can only be 1 */
    public void pageWrong(Integer count) {
        if (count != null && count > 0 && count <= limit && page > 1) {
            page = 1;
        }
    }


    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }

    public boolean isQueryTotal() {
        return queryTotal;
    }
    public void setQueryTotal(boolean queryTotal) {
        this.queryTotal = queryTotal;
    }


    public void setLimit(int limit) {
        this.limit = limit;
    }
    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getOffset() {
        return (page > 0) ? ((page - 1) * limit) : 0;
    }
    @Override
    public String toString() {
        return String.format("(page: %s, limit: %s, queryTotal: %s)", page, limit, queryTotal);
    }
}
