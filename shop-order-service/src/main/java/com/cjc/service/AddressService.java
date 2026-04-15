package com.cjc.service;

import com.cjc.pojo.TbAddress;
import com.cjc.vo.AddressVo;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService {

    /**
     * 获取用户所有地址列表
     */
    List<AddressVo> list(String userId);

    /**
     * 获取地址详情
     */
    AddressVo getById(Long id, String userId);

    /**
     * 新增地址
     */
    void add(TbAddress address);

    /**
     * 修改地址
     */
    void update(TbAddress address, String userId);

    /**
     * 删除地址
     */
    void delete(Long id, String userId);

    /**
     * 设置默认地址
     */
    void setDefault(Long id, String userId);

    /**
     * 获取用户默认地址
     */
    AddressVo getDefault(String userId);
}