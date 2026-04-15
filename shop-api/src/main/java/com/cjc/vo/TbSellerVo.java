package com.cjc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TbSellerVo {
    private String sellerId;

    private String name;

    private String nickName;

    private String password;

    private String salt;
}