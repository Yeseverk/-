package com.cjc.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "品牌实体类", description = "品牌表")
public class TbBrand {

    @Schema(name = "id", description = "品牌id")
    private Long id;

    @Schema(name = "name", description = "品牌名称")
    private String name;

    @Schema(name = "firstChar", description = "首字母")
    private String firstChar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(String firstChar) {
        this.firstChar = firstChar == null ? null : firstChar.trim();
    }
}