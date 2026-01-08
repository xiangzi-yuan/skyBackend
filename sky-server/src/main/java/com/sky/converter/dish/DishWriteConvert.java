package com.sky.converter.dish;

import com.sky.dto.dish.DishCreateDTO;
import com.sky.dto.dish.DishFlavorDTO;
import com.sky.dto.dish.DishUpdateDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import org.mapstruct.*;

import java.util.List;

/**
 * 菜品写转换器 - 负责菜品聚合根的写操作转换
 * 包含：Dish 和 DishFlavor 的 DTO -> Entity 转换
 */

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
    Dish fromCreateDTO(DishCreateDTO dto);

    /**
     * 修改菜品：DTO -> Entity（局部更新）
     * null 不覆盖；flavors 仍由 Service 处理
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
    void mergeUpdate(DishUpdateDTO dto, @MappingTarget Dish dish);
    // ==================== DishFlavor 转换 ====================

    /**
     * 口味 DTO -> Entity
     * id 和 dishId 由 Service 层填充
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dishId", ignore = true)
    })
    DishFlavor fromFlavorDTO(DishFlavorDTO dto);

    /**
     * 批量转换口味列表
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dishId", ignore = true)
    })
    List<DishFlavor> fromFlavorDTOList(List<DishFlavorDTO> dtoList);
}
