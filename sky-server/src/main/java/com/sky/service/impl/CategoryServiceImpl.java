package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.converter.CategoryReadConvert;
import com.sky.converter.CategoryWriteConvert;
import com.sky.dto.category.CategoryCreateDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.dto.category.CategoryUpdateDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.readmodel.category.CategoryDetailRM;
import com.sky.readmodel.category.CategoryPageRM;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.vo.category.CategoryDetailVO;
import com.sky.vo.category.CategorySimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 分类业务层实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;    private final DishMapper dishMapper;    private final SetmealMapper setmealMapper;    private final CategoryWriteConvert categoryWriteConvert;    private final CategoryReadConvert categoryReadConvert;
    /**
     * 新增分类
     * @param dto 新增分类DTO
     */
    @Override
    public void save(CategoryCreateDTO dto) {
        // DTO -> Entity
        Category category = categoryWriteConvert.fromCreateDTO(dto);

        // 业务字段
        category.setStatus(StatusConstant.DISABLE); // 新建默认禁用

        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * @param dto 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<CategoryPageRM> page = categoryMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), categoryReadConvert.toPageVOList(page.getResult()));
    }

    /**
     * 根据ID查询详情（用于编辑回显）
     * @param id 分类ID
     * @return 分类详情VO
     */
    @Override
    public CategoryDetailVO getById(Long id) {
        CategoryDetailRM detailRM = categoryMapper.getById(id);
        return categoryReadConvert.toDetailVO(detailRM);
    }

    /**
     * 根据ID删除分类（软删除）
     * @param id 分类ID
     */
    @Override
    public void deleteById(Long id) {
        // 查询当前分类是否关联了菜品
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 查询当前分类是否关联了套餐
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 软删除分类
        categoryMapper.softDeleteById(id, LocalDateTime.now());
    }

    /**
     * 修改分类
     * @param dto 修改分类DTO
     */
    @Override
    public void update(CategoryUpdateDTO dto) {
        // 先查询原实体
        Category category = categoryMapper.getEntityById(dto.getId());
        if (category == null) {
            throw new IllegalArgumentException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // DTO 合并到 Entity（只更新允许修改的字段）
        categoryWriteConvert.mergeUpdate(dto, category);

        categoryMapper.update(category);
    }

    /**
     * 启用/禁用分类
     * @param status 状态值
     * @param id 分类ID
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类列表（用于下拉选择）
     * @param type 分类类型（1-菜品分类，2-套餐分类，null-全部）
     * @return 分类简略信息列表
     */
    @Override
    public List<CategorySimpleVO> list(Integer type) {
        List<Category> categories = categoryMapper.list(type);
        return categoryReadConvert.toSimpleVOList(categories);
    }
}
