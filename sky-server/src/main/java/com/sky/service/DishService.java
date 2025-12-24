package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishDetailVO;

public interface DishService {

    void save(DishDTO dto);

    PageResult pageQuery(DishPageQueryDTO dto);
    
    /**
     * 根据ID查询菜品详情
     * @param id 菜品ID
     * @return 菜品详情VO（含口味列表）
     */
    DishDetailVO getDetailById(Long id);
}
