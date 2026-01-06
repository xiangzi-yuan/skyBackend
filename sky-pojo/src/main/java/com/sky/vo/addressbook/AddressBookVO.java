package com.sky.vo.addressbook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 地址响应 VO
 * 
 * <p>
 * 用于：详情查询、列表查询、默认地址查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 地址ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收货人 */
    private String consignee;

    /** 手机号 */
    private String phone;

    /** 性别 0-女 1-男 */
    private String sex;

    /** 省级区划编号 */
    private String provinceCode;

    /** 省级名称 */
    private String provinceName;

    /** 市级区划编号 */
    private String cityCode;

    /** 市级名称 */
    private String cityName;

    /** 区级区划编号 */
    private String districtCode;

    /** 区级名称 */
    private String districtName;

    /** 详细地址 */
    private String detail;

    /** 标签 */
    private String label;

    /** 是否默认 0-否 1-是 */
    private Integer isDefault;
}
