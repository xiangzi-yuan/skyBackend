package com.sky.converter;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DishWriteConvert {

    /**
     * 新增菜品：DTO -> Entity
     * 系统字段由 Service 填充；status 建议由后端决定；flavors 由 Service 处理（子表）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),

            // 业务控制字段：建议不信任前端
            @Mapping(target = "status", ignore = true),

            // 系统字段
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    Dish fromCreateDTO(DishDTO dto); // 把 DTO 转成一个新的 Dish 实体

    /**
     * 修改菜品：DTO -> Entity（局部更新）
     * null 不覆盖；flavors 仍由 Service 处理
     * 如果你们“编辑接口不允许改 status”，这里继续 ignore status
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),

            @Mapping(target = "status", ignore = true),

            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    void mergeUpdate(DishDTO dto, @MappingTarget Dish dish); // 把 dto 的非 null 字段拷贝到“已有的 dish 对象”上。
}
