package com.cjc.service;

import com.cjc.dto.SellerRegisterDto;
import com.cjc.dto.SellerUpdateDto;
import com.cjc.pojo.TbSeller;
import com.cjc.query.QueryParams;
import com.cjc.util.PageList;

public interface SellerService {

    /**
     * 根据用户名查询
     * @param sellerId
     * @return
     */
    TbSeller queryByUsername(String sellerId);

    /**
     * 商家注册
     * @param registerDto
     */
    void register(SellerRegisterDto registerDto);

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     */
    void updatePassword(String sellerId,String oldPassword, String newPassword);

    /**
     * 修改个人资料
     * @param updateDto
     * @param sellerId
     */
    void update(SellerUpdateDto updateDto, String sellerId);

    // ========== 运营商商家管理接口 ==========

    /**
     * 分页查询商家列表
     */
    PageList<TbSeller> queryPage(QueryParams<TbSeller> params);

    /**
     * 根据ID查询商家详情
     */
    TbSeller queryById(String sellerId);

    /**
     * 审核商家（修改状态）
     * @param sellerId 商家ID
     * @param status 状态：0待审核 1已审核 2审核未通过 3关闭
     */
    void audit(String sellerId, String status);
}
