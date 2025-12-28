package com.sky.converter;

import com.sky.dto.setmeal.SetmealCreateDTO;
import com.sky.dto.setmeal.SetmealDishDTO;
import com.sky.dto.setmeal.SetmealUpdateDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SetmealWriteConvert {
    /**
     * 新增菜品：DTO -> Entity
     * 系统字段由 Service 填充；status 建议由后端决定；flavors 由 Service 处理（子表）
     */
    @Mappings({
            // 业务控制字段：建议不信任前端
            @Mapping(target = "status", ignore = true),
    })
    Setmeal fromCreateDTO(SetmealCreateDTO dto);

    /**
     * 修改菜品：DTO -> Entity（局部更新）
     * null 不覆盖；flavors 仍由 Service 处理
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    void mergeUpdate(SetmealUpdateDTO dto, @MappingTarget Setmeal setmeal);

    // ==================== SetmealDishes 转换 ====================

    /**
     * 口味 DTO -> Entity
     * id 和 dishId 由 Service 层填充
     */
    SetmealDish fromDishDTO(SetmealDishDTO dto);

    /**
     * 批量转换套餐菜品列表
     */
    List<SetmealDish> fromDishDTOList(List<SetmealDishDTO> dtoList);
}
