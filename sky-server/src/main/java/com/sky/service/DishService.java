package com.sky.service;


import com.sky.dto.dish.DishCreateDTO;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.dto.dish.DishUpdateDTO;
import com.sky.result.PageResult;
import com.sky.vo.dish.DishDetailVO;

import java.util.List;

public interface DishService {

    void save(DishCreateDTO dto);

    PageResult pageQuery(DishPageQueryDTO dto);
    
    /**
     * 根据ID查询菜品详情
     * @param id 菜品ID
     * @return 菜品详情VO（含口味列表）
     */
    DishDetailVO getDetailById(Long id);

    /**
     * 根据分类ID查询起售中的菜品列表（User 端专用）
     * <p>业务规则：用户只能看到起售状态的菜品
     */
    List<DishDetailVO> listOnSaleByCategoryId(Long categoryId);

    /**
     * 根据分类ID查询所有菜品列表（Admin 端专用）
     * <p>包含所有状态的菜品，用于管理
     */
    List<DishDetailVO> listAllByCategoryId(Long categoryId);

    void delete(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void changeDish(DishUpdateDTO dto);


}
