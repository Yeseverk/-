package com.cjc.service.impl;

import com.cjc.dto.SellerRegisterDto;
import com.cjc.dto.SellerUpdateDto;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbSellerMapper;
import com.cjc.pojo.TbSeller;
import com.cjc.pojo.TbSellerExample;
import com.cjc.query.QueryParams;
import com.cjc.service.SellerService;
import com.cjc.util.PageList;
import com.github.pagehelper.PageHelper;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SellerServiceImpl implements SellerService {

    @Autowired
    private TbSellerMapper sellerMapper;

    @Override
    public TbSeller queryByUsername(String sellerId) {
        TbSellerExample tbSellerExample = new TbSellerExample();
        tbSellerExample.createCriteria().andSellerIdEqualTo(sellerId);
        List<TbSeller> sellers = sellerMapper.selectByExample(tbSellerExample);
        if(sellers!=null && sellers.size()>0){
            return sellers.get(0);
        }
        return null;
    }

    @Override
    public void register(SellerRegisterDto registerDto) {
        // 1. 检查用户名（sellerId）是否已存在
        TbSeller seller = sellerMapper.selectByPrimaryKey(registerDto.getSellerId());
        if (seller != null) {
            throw new BusinessException("用户名已存在");
        }
        // 2. 生成 salt
        String salt = UUID.randomUUID().toString().replace("-", "");
        // 3. 【核心修改】使用 Shiro 原生的 SimpleHash 进行加密
        // 参数依次为：算法名称，原始密码，盐值（必须转为 ByteSource），散列次数
        String md5Password = new SimpleHash(
                "md5",
                registerDto.getPassword(),
                ByteSource.Util.bytes(salt),
                7
        ).toHex();
        // 4. 设置 TbSeller 实体
        TbSeller newSeller = new TbSeller();
        newSeller.setSellerId(registerDto.getSellerId());  // username → sellerId
        newSeller.setPassword(md5Password);
        newSeller.setSalt(salt);
        newSeller.setName(registerDto.getName());
        newSeller.setNickName(registerDto.getNickName());
        newSeller.setMobile(registerDto.getMobile());
        newSeller.setTelephone(registerDto.getTelephone());
        newSeller.setAddressDetail(registerDto.getAddressDetail());
        newSeller.setLinkmanQq(registerDto.getLinkmanQq());
        newSeller.setLinkmanMobile(registerDto.getLinkmanMobile());
        newSeller.setLinkmanEmail(registerDto.getLinkmanEmail());
        newSeller.setLicenseNumber(registerDto.getLicenseNumber());
        newSeller.setTaxNumber(registerDto.getTaxNumber());
        newSeller.setOrgNumber(registerDto.getOrgNumber());
        newSeller.setLegalPerson(registerDto.getLegalPerson());
        newSeller.setLegalPersonCardId(registerDto.getLegalPersonCardId());
        newSeller.setBankName(registerDto.getBankName());
        newSeller.setBankUser(registerDto.getBankUser());
        // 5. 设置默认状态：新注册商家需要运营商审核
        newSeller.setStatus("0");  // 0=待审核
        newSeller.setCreateTime(new Date());
        // 6. 插入数据库
        sellerMapper.insert(newSeller);
    }

    @Override
    public void updatePassword(String sellerId,String oldPassword, String newPassword) {
        TbSeller tbSeller =  sellerMapper.queryById(sellerId);
        if (tbSeller == null) {
            throw new BusinessException("用户不存在");
        }
        String salt = tbSeller.getSalt();
        String encryptedOldPassword = new SimpleHash(
                "md5",
                oldPassword,
                ByteSource.Util.bytes(salt),
                7
        ).toHex();
        if (!encryptedOldPassword.equals(tbSeller.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        String encryptedNewPassword = new SimpleHash(
                "md5",
                newPassword,
                ByteSource.Util.bytes(salt),
                7
        ).toHex();
        if (encryptedNewPassword.equals(tbSeller.getPassword())){
            throw new BusinessException("修改后的密码不能与原密码一致！");
        }
        tbSeller.setPassword(encryptedNewPassword);
        TbSeller updateSeller = new TbSeller();

        // 2. 【核心修复】一定要把主键塞进去！否则 where 条件就是 null
        updateSeller.setSellerId(sellerId);

        // 3. 把新密码塞进去
        updateSeller.setPassword(encryptedNewPassword);

        // 4. 【核心修复】一定要用 Selective 方法！
        // 这样 MyBatis 就只会生成 UPDATE tb_seller SET password = ? WHERE seller_id = ?
        // 绝对不会去乱动其他的业务字段
        sellerMapper.updateByPrimaryKeySelective(updateSeller);
    }

    @Override
    public void update(SellerUpdateDto updateDto, String sellerId) {
        // 1. 创建一个干净的实体对象，用来承载要更新的数据
        TbSeller updateSeller = new TbSeller();
        // 2. 把 DTO 里的数据"倒"进实体对象里
        // 使用 Spring 核心包提供的 BeanUtils 工具类，它会自动把名字相同的属性拷贝过去，省去了十几行 set 代码
        BeanUtils.copyProperties(updateDto, updateSeller);
        // 3. 【最关键的一步】把身份 ID 塞进去！这决定了最终 SQL 语句的 WHERE 条件
        updateSeller.setSellerId(sellerId);
        // 4. 调用动态更新方法
        // MyBatis 会自动判断：updateSeller 里面哪些字段有值，就去 UPDATE 哪些字段；为 null 的字段直接忽略，绝不会覆盖掉数据库里原有（未修改）的数据。
        sellerMapper.updateByPrimaryKeySelective(updateSeller);
    }

    // ========== 运营商商家管理接口实现 ==========

    @Override
    public PageList<TbSeller> queryPage(QueryParams<TbSeller> params) {
        // 分页设置
        com.github.pagehelper.Page<TbSeller> page = 
            com.github.pagehelper.PageHelper.startPage(params.getCurrentPage(), params.getPageSize());
        
        // 构建查询条件
        TbSellerExample example = new TbSellerExample();
        TbSellerExample.Criteria criteria = example.createCriteria();
        
        if (params.getParams() != null) {
            // 公司名称模糊查询
            if (params.getParams().getName() != null && params.getParams().getName().trim().length() > 0) {
                criteria.andNameLike("%" + params.getParams().getName().trim() + "%");
            }
            // 店铺名称模糊查询
            if (params.getParams().getNickName() != null && params.getParams().getNickName().trim().length() > 0) {
                criteria.andNickNameLike("%" + params.getParams().getNickName().trim() + "%");
            }
            // 状态精确查询
            if (params.getParams().getStatus() != null && params.getParams().getStatus().trim().length() > 0) {
                criteria.andStatusEqualTo(params.getParams().getStatus().trim());
            }
        }
        
        // 按创建时间倒序
        example.setOrderByClause("create_time DESC");
        
        List<TbSeller> rows = sellerMapper.selectByExample(example);
        
        return new PageList<>(page.getTotal(), rows);
    }

    @Override
    public TbSeller queryById(String sellerId) {
        TbSeller seller = sellerMapper.selectByPrimaryKey(sellerId);
        if (seller != null) {
            // 脱敏处理：清除密码和盐值
            seller.setPassword(null);
            seller.setSalt(null);
        }
        return seller;
    }

    @Override
    public void audit(String sellerId, String status) {
        TbSeller seller = sellerMapper.selectByPrimaryKey(sellerId);
        if (seller == null) {
            throw new BusinessException("商家不存在");
        }
        
        // 更新状态
        TbSeller updateSeller = new TbSeller();
        updateSeller.setSellerId(sellerId);
        updateSeller.setStatus(status);
        
        sellerMapper.updateByPrimaryKeySelective(updateSeller);
    }
}
