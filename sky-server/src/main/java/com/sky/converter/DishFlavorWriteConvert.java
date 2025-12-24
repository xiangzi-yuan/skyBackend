package com.sky.converter;

import com.sky.dto.DishFlavorDTO;
import com.sky.entity.DishFlavor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DishFlavorWriteConvert {

    @Mappings({
            @Mapping(target = "id", ignore = true),      // 新增时由数据库生成
            @Mapping(target = "dishId", ignore = true)   // 由 Service 根据 dishId 填
    })
    DishFlavor fromDTO(DishFlavorDTO dto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dishId", ignore = true)
    })
    List<DishFlavor> fromDTOList(List<DishFlavorDTO> dtoList);
}
