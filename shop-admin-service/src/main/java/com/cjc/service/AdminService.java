package com.cjc.service;

import com.cjc.pojo.TbAdmin;

public interface AdminService {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    TbAdmin queryByUsername(String username);

    /**
     * 保存管理员
     * @param admin
     */
    void save(TbAdmin admin);
}
