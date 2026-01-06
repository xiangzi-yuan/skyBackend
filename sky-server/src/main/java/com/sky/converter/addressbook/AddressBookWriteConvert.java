package com.sky.converter.addressbook;

import com.sky.dto.addressbook.AddressBookCreateDTO;
import com.sky.dto.addressbook.AddressBookUpdateDTO;
import com.sky.entity.AddressBook;
import org.mapstruct.*;

/**
 * 地址簿写转换器 - 负责写操作转换
 * 
 * <p>
 * 包含：DTO -> Entity 的转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressBookWriteConvert {

    /**
     * 新增地址：CreateDTO -> Entity
     * 
     * <p>
     * 忽略的字段（由 Service 层填充）：
     * <ul>
     * <li>id - 数据库自动生成</li>
     * <li>userId - 从登录上下文获取</li>
     * <li>isDefault - 新增默认为 0，由单独接口设置</li>
     * </ul>
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "userId", ignore = true),
            @Mapping(target = "isDefault", ignore = true)
    })
    AddressBook fromCreateDTO(AddressBookCreateDTO dto);

    /**
     * 修改地址：UpdateDTO -> Entity（局部更新）
     * 
     * <p>
     * null 值不覆盖已有值
     * <p>
     * 忽略的字段：
     * <ul>
     * <li>id - 通过参数传入</li>
     * <li>userId - 安全考虑不允许修改</li>
     * <li>isDefault - 由单独接口管理</li>
     * </ul>
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "userId", ignore = true),
            @Mapping(target = "isDefault", ignore = true)
    })
    void mergeUpdate(AddressBookUpdateDTO dto, @MappingTarget AddressBook addressBook);
}
