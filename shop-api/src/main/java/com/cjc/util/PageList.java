package com.cjc.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 总条数
 * 每页数据
 */
public class PageList<T> {

    private Long total = 0L;  // 总条数
    private List<T> rows = new ArrayList<>(); // 每页数据

    public PageList(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageList() {
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "PageList{" +
                "total=" + total +
                ", rows=" + rows +
                '}';
    }
}
