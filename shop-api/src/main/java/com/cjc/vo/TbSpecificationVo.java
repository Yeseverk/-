package com.cjc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TbSpecificationVo {
    private Long id;

    // 集合，根据规格id查询的option
    private List<TbSpecificationOptionVo> options = new ArrayList<>();

    private String specName;


}