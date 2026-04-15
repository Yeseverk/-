package com.cjc.query;

/**
 * 分页+ 查询条件的参数
 */
public class QueryParams<T> {

    private Integer currentPage; // 当前页
    private Integer pageSize; // 每页条数
    private  T params;

    public QueryParams(Integer currentPage, Integer pageSize, T params) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.params = params;
    }

    public QueryParams() {
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public T getParams() {
        return params;
    }

    public void setParams(T params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", params=" + params +
                '}';
    }
}
