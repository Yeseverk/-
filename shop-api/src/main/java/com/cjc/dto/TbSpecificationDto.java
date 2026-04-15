package com.cjc.dto;

import com.cjc.pojo.TbSpecificationOption;
import com.cjc.vo.TbSpecificationOptionVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TbSpecificationDto {
    private Long id;

    // 集合，根据规格id查询的option
    private List<TbSpecificationOption> options = new ArrayList<>();

    private String specName;


}