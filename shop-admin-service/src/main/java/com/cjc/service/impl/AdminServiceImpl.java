package com.cjc.service.impl;

import com.cjc.mapper.TbAdminMapper;
import com.cjc.pojo.TbAdmin;
import com.cjc.pojo.TbAdminExample;
import com.cjc.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private TbAdminMapper adminMapper;

    @Override
    public TbAdmin queryByUsername(String username) {

        TbAdminExample example = new TbAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<TbAdmin> tbAdmins = adminMapper.selectByExample(example);

        if(tbAdmins!=null && tbAdmins.size()>0){
            return tbAdmins.get(0);
        }
        return null;
    }

    @Override
    public void save(TbAdmin admin) {
        adminMapper.insert(admin);
    }
}
