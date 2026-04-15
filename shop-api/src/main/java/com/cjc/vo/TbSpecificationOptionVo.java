package com.cjc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TbSpecificationOptionVo {
    private Long id;

    private String optionName;

    private Long specId;

    private Integer orders;


}