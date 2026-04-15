package com.cjc.service;

import com.cjc.pojo.TbBrand;
import com.cjc.query.QueryParams;
import com.cjc.util.PageList;
import com.cjc.vo.TbBrandVo;

import java.util.List;

public interface BrandService {


    /**
     * 条件查询 + 分页
     *
     * 返回结果：
     *     总条数 Long， 每页数据 list<T>   封装一下。
     *
     *  参数：
     *      当前页：currentPage
     *      每页条数： pageSize
     *      查询条件：query
     *
     *  分页自己实现：
     *    两个sql： 总条数(query条件)， 每页数据
     *
     *    dto: 数据传输对象
     *    vo: 给前端展示的对象
     *    接收分页参数的对象
     *
     */
    PageList<TbBrandVo> queryPage(QueryParams<TbBrand> queryParams);

    /**
     * 添加操作
     * @param tbBrand
     */
    void insert(TbBrand tbBrand);

    /**
     * 修改操作
     * @param tbBrand
     */
    void update(TbBrand tbBrand);

    /**
     * 删除操作
     * @param id
     */
    void delete(Long id);

    /**
     * 批量删除
     * @param ids
     */
    void batchDelete(List<Long> ids);

    /**
     * 查询所有
     * @return
     */
    List<TbBrandVo> queryAll();
}
