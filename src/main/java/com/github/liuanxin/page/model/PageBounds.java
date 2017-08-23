package com.github.liuanxin.page.model;

import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;

public class PageBounds extends RowBounds implements Serializable {

    private int page = 1;
    private int limit = 15;
    /** 在 app 中(Android, iOS ...)是不需要查询总条数的 */
    private boolean queryTotal = false;
    /** 如果总条数是 10, 每页 15 条, 如果此时传进来的 page 是 2, 那这是错的, 只能是 1 */
    private boolean checkPage = false;

    public PageBounds() {}
    public PageBounds(int limit) {
        this.limit = limit;
    }
    public PageBounds(int page, int limit) {
        this.page = page;
        this.limit = limit;
        queryTotal = true;
    }

    public PageBounds(int limit, boolean checkPage) {
        this.limit = limit;
        this.checkPage = checkPage;
    }
    public PageBounds(int page, int limit, boolean checkPage) {
        this.page = page;
        this.limit = limit;
        this.checkPage = checkPage;
        queryTotal = true;
    }


    public boolean notNeedPage() {
        return getOffset() == RowBounds.NO_ROW_OFFSET && limit == RowBounds.NO_ROW_LIMIT;
    }

    /**
     * If count = 10, limit = 15, But incoming param page value was 2, It's wrong, Can only be 1
     *
     * @param count select count(1)
     */
    public void pageWrong(Integer count) {
        if (checkPage && count != null && count > 0 && count <= limit && page > 1) {
            page = 1;
        }
    }


    public void setPage(int page) {
        this.page = page;
    }
    public int getPage() { return page; }

    public void setQueryTotal(boolean queryTotal) {
        this.queryTotal = queryTotal;
    }
    public boolean isQueryTotal() { return queryTotal; }

    public void setCheckPage(boolean checkPage) {
        this.checkPage = checkPage;
    }
    public boolean isCheckPage() { return checkPage; }

    public void setLimit(int limit) { this.limit = limit; }
    @Override
    public int getLimit() { return limit; }

    @Override
    public int getOffset() { return (page > 0) ? ((page - 1) * limit) : 0; }

    @Override
    public String toString() {
        return String.format("(page: %s, limit: %s, queryTotal: %s, checkPage: %s)",
                page, limit, queryTotal, checkPage);
    }
}
