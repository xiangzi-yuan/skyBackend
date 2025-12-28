package com.sky.converter;

import com.sky.entity.DishFlavor;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.readmodel.dish.DishPageRM;
import com.sky.vo.dish.DishDetailVO;
import com.sky.vo.dish.DishFlavorVO;
import com.sky.vo.dish.DishPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DishReadConvert {

    DishPageVO toPageVO(DishPageRM rm);
    
    /**
     * 将 DishDetailRM 转换为 DishDetailVO
     * 注意：需要手动处理 flavors 的转换
     */
    DishDetailVO toDetailVO(DishDetailRM rm);
    
    /**
     * 将 DishFlavor Entity 转换为 DishFlavorVO
     * 只保留 name 和 value，去掉 id 和 dishId
     */
    DishFlavorVO toFlavorVO(DishFlavor flavor);
    
    /**
     * 批量转换口味列表
     */
    List<DishFlavorVO> toFlavorVOList(List<DishFlavor> flavors);
}
