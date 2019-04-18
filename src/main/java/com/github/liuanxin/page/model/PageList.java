package com.github.liuanxin.page.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author https://github.com/liuanxin
 */
public class PageList<E> extends ArrayList<E> {

    /** save with: select count(*) from ... */
    private int total;

    public PageList() {
        super();
    }
    public PageList(Collection<? extends E> collection, int total) {
        super(collection);
        this.total = total;
    }

    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
}
