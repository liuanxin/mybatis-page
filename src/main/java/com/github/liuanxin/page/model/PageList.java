package com.github.liuanxin.page.model;

import java.util.ArrayList;
import java.util.Collection;

public class PageList<E> extends ArrayList<E> {

    private int count;

    public PageList() {
        super();
    }
    public PageList(Collection<? extends E> c, int total) {
        super(c);
        this.count = total;
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}
