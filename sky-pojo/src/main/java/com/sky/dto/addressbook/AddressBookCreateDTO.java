package com.sky.dto.addressbook;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 新增地址 DTO
 * 
 * <p>
 * 规范说明：
 * <ul>
 * <li>不包含 id（数据库自动生成）</li>
 * <li>不包含 userId（从登录上下文获取）</li>
 * <li>不包含 isDefault（由单独的设置默认接口管理）</li>
 * </ul>
 */
@Data
public class AddressBookCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收货人（必填） */
    @NotBlank(message = "收货人不能为空")
    private String consignee;

    /** 手机号（必填） */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 性别 0-女 1-男（必填） */
    @NotBlank(message = "性别不能为空")
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

    /** 详细地址（必填） */
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 标签（如：家、公司） */
    private String label;
}
