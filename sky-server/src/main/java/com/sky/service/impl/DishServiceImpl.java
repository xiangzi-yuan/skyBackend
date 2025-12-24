package com.sky.service.impl;

import com.sky.constant.PasswordConstant;
import com.sky.constant.PwdChangedConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.converter.DishFlavorWriteConvert;
import com.sky.converter.DishReadConvert;
import com.sky.converter.DishWriteConvert;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishWriteConvert dishWriteConvert;
    @Autowired
    private DishFlavorWriteConvert dishFlavorWriteConvert;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    public void save(DishDTO dto) {
        Dish dish = dishWriteConvert.fromCreateDTO(dto);

        dish.setStatus(StatusConstant.DISABLE);

        dishMapper.insert(dish);

        // 2. 获取自增的菜品ID（@Options 配置会自动回填到 dish.id）
        Long dishId = dish.getId();

        // 3. 处理口味数据
        if (dto.getFlavors() != null && !dto.getFlavors().isEmpty()) {
            List<DishFlavor> flavors = dishFlavorWriteConvert.fromDTOList(dto.getFlavors());
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

}
