package com.cjc.vo;

import com.cjc.pojo.TbAddress;

/**
 * 收货地址VO
 */
public class AddressVo extends TbAddress {

    // 省名称（前端查询填充）
    private String provinceName;

    // 市名称
    private String cityName;

    // 区/县名称
    private String townName;

    // 完整地址（省+市+区+详细地址）
    private String fullAddress;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (provinceName != null) sb.append(provinceName);
        if (cityName != null) sb.append(cityName);
        if (townName != null) sb.append(townName);
        if (getAddress() != null) sb.append(getAddress());
        return sb.toString();
    }
}