package com.cjc.service.impl;

import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbAddressMapper;
import com.cjc.pojo.TbAddress;
import com.cjc.pojo.TbAddressExample;
import com.cjc.service.AddressService;
import com.cjc.vo.AddressVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private TbAddressMapper addressMapper;

    @Override
    public List<AddressVo> list(String userId) {
        TbAddressExample example = new TbAddressExample();
        example.createCriteria().andUserIdEqualTo(userId);
        example.setOrderByClause("is_default DESC, create_date DESC");

        List<TbAddress> addresses = addressMapper.selectByExample(example);
        return convertToVoList(addresses);
    }

    @Override
    public AddressVo getById(Long id, String userId) {
        TbAddress address = addressMapper.selectByPrimaryKey(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }

        // 权限校验
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权访问");
        }

        return convertToVo(address);
    }

    @Override
    @Transactional
    public void add(TbAddress address) {
        address.setCreateDate(new Date());

        // 如果设置为默认，先清除其他默认
        if ("1".equals(address.getIsDefault())) {
            clearDefault(address.getUserId());
        } else {
            // 默认设置为0
            address.setIsDefault("0");
        }

        addressMapper.insertSelective(address);
    }

    @Override
    @Transactional
    public void update(TbAddress address, String userId) {
        TbAddress existing = addressMapper.selectByPrimaryKey(address.getId());
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }

        // 权限校验
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        // 如果设置为默认，先清除其他默认
        if ("1".equals(address.getIsDefault())) {
            clearDefault(userId);
        }

        addressMapper.updateByPrimaryKeySelective(address);
    }

    @Override
    @Transactional
    public void delete(Long id, String userId) {
        TbAddress address = addressMapper.selectByPrimaryKey(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }

        // 权限校验
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        addressMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void setDefault(Long id, String userId) {
        TbAddress address = addressMapper.selectByPrimaryKey(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }

        // 权限校验
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        // 先清除其他默认
        clearDefault(userId);

        // 设置当前为默认
        TbAddress update = new TbAddress();
        update.setId(id);
        update.setIsDefault("1");
        addressMapper.updateByPrimaryKeySelective(update);
    }

    @Override
    public AddressVo getDefault(String userId) {
        TbAddressExample example = new TbAddressExample();
        example.createCriteria()
            .andUserIdEqualTo(userId)
            .andIsDefaultEqualTo("1");

        List<TbAddress> addresses = addressMapper.selectByExample(example);
        if (addresses != null && !addresses.isEmpty()) {
            return convertToVo(addresses.get(0));
        }

        // 如果没有默认地址，返回第一个地址
        List<AddressVo> all = list(userId);
        if (all != null && !all.isEmpty()) {
            return all.get(0);
        }

        return null;
    }

    /**
     * 清除用户所有默认地址
     */
    private void clearDefault(String userId) {
        TbAddressExample example = new TbAddressExample();
        example.createCriteria()
            .andUserIdEqualTo(userId)
            .andIsDefaultEqualTo("1");

        TbAddress update = new TbAddress();
        update.setIsDefault("0");

        addressMapper.updateByExampleSelective(update, example);
    }

    /**
     * 转换为VO列表
     */
    private List<AddressVo> convertToVoList(List<TbAddress> addresses) {
        return addresses.stream()
            .map(this::convertToVo)
            .toList();
    }

    /**
     * 转换为VO
     */
    private AddressVo convertToVo(TbAddress address) {
        AddressVo vo = new AddressVo();
        BeanUtils.copyProperties(address, vo);
        // 省市县名称需要前端自己根据ID查询或后端对接地区表
        return vo;
    }
}