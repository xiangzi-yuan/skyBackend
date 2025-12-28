package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.converter.DishReadConvert;
import com.sky.converter.DishWriteConvert;
import com.sky.dto.DishCreateDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.DishUpdateDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.readmodel.dish.DishPageRM;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishDetailVO;
import com.sky.vo.DishFlavorVO;
import com.sky.vo.DishPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishWriteConvert dishWriteConvert;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishReadConvert dishReadConvert;
    @Autowired
    private SetmealDishMapper setmealDishMapper;



    @Override
    @Transactional
    public void save(DishCreateDTO dto) {
        Dish dish = dishWriteConvert.fromCreateDTO(dto);

        dish.setStatus(StatusConstant.DISABLE);

        dishMapper.insert(dish);

        // 2. 获取自增的菜品ID（@Options 会自动回填到 dish.id）
        Long dishId = dish.getId();

        // 3. 处理口味数据
        if (dto.getFlavors() != null && !dto.getFlavors().isEmpty()) {
            List<DishFlavor> flavors = dishWriteConvert.fromFlavorDTOList(dto.getFlavors());
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DishPageRM> page = dishMapper.pageQuery(dto);
        long total = page.getTotal();
        List<DishPageVO> records = page.getResult()
                .stream()
                .map(dishReadConvert::toPageVO)
                .collect(Collectors.toList());

        return new PageResult(total, records);
    }

    /**
     * 根据ID查询菜品详情
     *
     * <p>查询流程：
     * <ol>
     *   <li>查询菜品基础信息（包含分类名称）</li>
     *   <li>查询菜品关联的口味列表</li>
     *   <li>组装并转换为 VO 返回</li>
     * </ol>
     *
     * @param id 菜品ID
     * @return 菜品详情 VO（包含基础信息和口味列表）
     */
    @Override
    public DishDetailVO getDetailById(Long id) {
        // 1. 查询菜品基本信息（含分类名称）
        DishDetailRM dishDetailRM = dishMapper.getDetailById(id);

        // 2. 查询关联的口味列表
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);

        // 3. 转换为 VO
        DishDetailVO vo = dishReadConvert.toDetailVO(dishDetailRM);

        // 4. 转换并设置口味列表
        List<DishFlavorVO> flavorVOs = dishReadConvert.toFlavorVOList(flavors);
        vo.setFlavors(flavorVOs);

        return vo;
    }

    @Override
    public List<DishDetailVO> getByCategoryId(Long categoryId) {

        List<DishDetailRM> dishDetailRMList = dishMapper.getByCategoryId(categoryId);
        List<DishDetailVO> voList = dishDetailRMList.stream()
                .map(dishReadConvert::toDetailVO)
                .toList();
        return voList;
    }

    /**
     * 删除菜品（支持批量）
     *
     * <p>业务规则：
     * <ul>
     *   <li>起售中的菜品不能删除</li>
     *   <li>被套餐关联的菜品不能删除</li>
     *   <li>需要同时删除关联的口味数据</li>
     * </ul>
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            DishDetailRM dishRM = dishMapper.getDetailById(id);
            if (dishRM == null) {
                continue;
            }
            if (Objects.equals(dishRM.getStatus(), StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 若存在外键约束，应先删子表再删主表
        dishFlavorMapper.delete(ids);
        dishMapper.delete(ids);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateStatus(dish);
    }

    /**
     * 修改菜品：UpdateDTO -> Entity（局部更新）
     */
    @Override
    @Transactional
    public void changeDish(DishUpdateDTO dto) {
        Long dishId = dto.getId();
        if (dishId == null) {
            throw new IllegalArgumentException("dishId 不能为空");
        }

        // 1) 更新主表
        Dish dish = new Dish();
        dish.setId(dishId);
        dishWriteConvert.mergeUpdate(dto, dish);
        int rows = dishMapper.update(dish);
        if (rows != 1) {
            throw new RuntimeException("菜品不存在或更新失败, id=" + dishId);
        }

        // 2) 子表：先删（未传口味也清空）
        dishFlavorMapper.deleteByDishId(dishId);

        // 3) 有口味则插入；无/空则保持清空
        if (dto.getFlavors() == null || dto.getFlavors().isEmpty()) {
            return;
        }
        List<DishFlavor> flavors = dishWriteConvert.fromFlavorDTOList(dto.getFlavors());
        flavors.forEach(f -> f.setDishId(dishId));

        flavors = flavors.stream()
                .filter(f -> f.getName() != null && !f.getName().isBlank())
                .filter(f -> f.getValue() != null && !f.getValue().isBlank())
                .toList();

        if (!flavors.isEmpty()) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }



}


