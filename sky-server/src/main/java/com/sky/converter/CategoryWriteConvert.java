package com.sky.converter;

import com.sky.dto.category.CategoryCreateDTO;
import com.sky.dto.category.CategoryUpdateDTO;
import com.sky.entity.Category;
import org.mapstruct.*;

/**
 * 分类写转换器 - 负责分类的写操作转换
 * 
 * <p>包含：DTO -> Entity 的转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryWriteConvert {

    /**
     * 新增分类：CreateDTO -> Entity
     * 
     * <p>忽略的字段（由 Service 层填充）：
     * <ul>
     *   <li>id - 数据库自动生成</li>
     *   <li>status - 新建默认禁用，后端控制</li>
     *   <li>createTime/updateTime - 系统时间</li>
     *   <li>createUser/updateUser - 当前登录用户</li>
     *   <li>isDeleted/deleteTime - 软删除字段</li>
     * </ul>
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "deleteTime", ignore = true)
    })
    Category fromCreateDTO(CategoryCreateDTO dto);

    /**
     * 修改分类：UpdateDTO -> Entity（局部更新）
     * 
     * <p>null 值不覆盖已有值
     * <p>忽略的字段：
     * <ul>
     *   <li>id - 通过 DTO 传入，不走映射</li>
     *   <li>type - 分类类型创建后不可修改</li>
     *   <li>status - 由单独的启用/禁用接口管理</li>
     *   <li>系统字段 - 由 Service 层填充</li>
     * </ul>
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "type", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "deleteTime", ignore = true)
    })
    void mergeUpdate(CategoryUpdateDTO dto, @MappingTarget Category category);
}
