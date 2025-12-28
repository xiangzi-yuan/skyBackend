package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.DishDetailVO;
import com.sky.vo.SetmealDetailVO;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐
     * @param dto
     */
    void save(SetmealCreateDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO dto);

    /**
     * 修改套餐
     * @param dto
     */
    void changeSetmeal(SetmealUpdateDTO dto);

    /**
     * 根据ID查询套餐详情
     * @param id 菜品ID
     * @return 菜品详情VO（含口味列表）
     */
    SetmealDetailVO getDetailById(Long id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 套餐起售停售
     * @param id
     * @param status
     */
    void updateStatus(Long id, Integer status);


}
