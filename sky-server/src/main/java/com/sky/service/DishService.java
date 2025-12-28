package com.sky.service;


import com.sky.dto.DishCreateDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.DishUpdateDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishDetailVO;

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

    List<DishDetailVO> getByCategoryId(Long categoryId);

    void delete(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void changeDish(DishUpdateDTO dto);


}
