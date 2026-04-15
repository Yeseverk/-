package com.cjc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TbAdminVo {
    private Long id;

    private String username;

    private String passsword;

    private String salt;
   }