package com.sky.service;

import com.sky.dto.category.CategoryCreateDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.dto.category.CategoryUpdateDTO;
import com.sky.result.PageResult;
import com.sky.vo.category.CategoryDetailVO;
import com.sky.vo.category.CategorySimpleVO;

import java.util.List;

/**
 * 分类业务接口
 */
public interface CategoryService {

    /**
     * 新增分类
     * @param dto 新增分类DTO
     */
    void save(CategoryCreateDTO dto);

    /**
     * 分页查询
     * @param dto 分页查询条件
     * @return 分页结果（包含 CategoryPageVO 列表）
     */
    PageResult pageQuery(CategoryPageQueryDTO dto);

    /**
     * 根据ID查询详情（用于编辑回显）
     * @param id 分类ID
     * @return 分类详情VO
     */
    CategoryDetailVO getById(Long id);

    /**
     * 根据ID删除分类（软删除）
     * @param id 分类ID
     */
    void deleteById(Long id);

    /**
     * 修改分类
     * @param dto 修改分类DTO
     */
    void update(CategoryUpdateDTO dto);

    /**
     * 启用/禁用分类
     * @param status 状态值
     * @param id 分类ID
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据类型查询分类列表（用于下拉选择）
     * @param type 分类类型
     * @return 分类简略信息列表
     */
    List<CategorySimpleVO> list(Integer type);
}
