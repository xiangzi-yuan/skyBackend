package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.CategoryConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.converter.DishReadConvert;
import com.sky.converter.DishWriteConvert;
import com.sky.dto.dish.DishCreateDTO;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.dto.dish.DishUpdateDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.readmodel.dish.DishPageRM;
import com.sky.readmodel.dish.DishSetmealRelationRM;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.dish.DishDetailVO;
import com.sky.vo.dish.DishFlavorVO;
import com.sky.vo.dish.DishPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
     * <p>
     * 查询流程：
     * <ol>
     * <li>查询菜品基础信息（包含分类名称）</li>
     * <li>查询菜品关联的口味列表</li>
     * <li>组装并转换为 VO 返回</li>
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
    public List<DishDetailVO> listOnSaleByCategoryId(Long categoryId) {
        return listByCategoryIdInternal(categoryId, StatusConstant.ENABLE);
    }

    @Override
    public List<DishDetailVO> listAllByCategoryId(Long categoryId) {
        return listByCategoryIdInternal(categoryId, null);
    }

    /**
     * 内部复用方法：根据分类ID查询菜品列表
     * 
     * @param categoryId 分类ID
     * @param status     状态（null 表示不过滤）
     */
    private List<DishDetailVO> listByCategoryIdInternal(Long categoryId, Integer status) {
        List<DishDetailRM> dishDetailRMList = dishMapper.getByCategoryId(categoryId, status, CategoryConstant.DISH);
        return dishDetailRMList.stream()
                .map(rm -> {
                    DishDetailVO vo = dishReadConvert.toDetailVO(rm);
                    // 查询并设置口味列表
                    List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(rm.getId());
                    List<DishFlavorVO> flavorVOs = dishReadConvert.toFlavorVOList(flavors);
                    vo.setFlavors(flavorVOs);
                    return vo;
                })
                .toList();
    }

    /**
     * 删除菜品（支持批量）- 软删除
     *
     * <p>
     * 业务规则：
     * <ul>
     * <li>起售中的菜品不能删除</li>
     * <li>被套餐关联的菜品不能删除</li>
     * <li>采用软删除，保留历史数据用于订单统计</li>
     * </ul>
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Long> idList = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (idList.isEmpty()) {
            return;
        }

        Integer onSaleCount = dishMapper.countByIdsAndStatus(idList, StatusConstant.ENABLE);
        if (onSaleCount != null && onSaleCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        List<DishSetmealRelationRM> relations = setmealDishMapper.getDishSetmealRelations(idList);
        if (relations != null && !relations.isEmpty()) {

            // 按菜品ID分组（避免同名菜品被错误合并）
            Map<Long, List<DishSetmealRelationRM>> groupedByDishId = relations.stream()
                    .filter(r -> r.getDishId() != null) // 防御：避免 key 为 null
                    .collect(Collectors.groupingBy(DishSetmealRelationRM::getDishId));

            StringBuilder sb = new StringBuilder(MessageConstant.DISH_BE_RELATED_BY_SETMEAL_DETAIL);

            // 稳定输出顺序（否则 HashMap 遍历顺序不固定）
            groupedByDishId.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // 按 dishId 排序
                    .forEachOrdered(entry -> {
                        Long dishId = entry.getKey();
                        List<DishSetmealRelationRM> relList = entry.getValue();

                        // 展示用菜品名：取本组第一个非空名称
                        String dishName = relList.stream()
                                .map(DishSetmealRelationRM::getDishName)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse("未知菜品(" + dishId + ")");

                        // 套餐名：去重 + 连接
                        String setmealNames = relList.stream()
                                .map(DishSetmealRelationRM::getSetmealName)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.joining("、"));

                        sb.append("【").append(dishName).append("→").append(setmealNames).append("】");
                    });

            throw new DeletionNotAllowedException(sb.toString());
        }

        // 软删除：标记 is_deleted = 1，保留数据用于历史订单查询
        // 注意：口味数据不删除，因为订单详情可能需要展示
        dishMapper.softDelete(idList, LocalDateTime.now());
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

        // 1) 更新主表
        Dish dish = new Dish();
        dish.setId(dishId);
        dishWriteConvert.mergeUpdate(dto, dish);
        int rows = dishMapper.update(dish);
        if (rows != 1) {
            throw new IllegalArgumentException(MessageConstant.DISH_NOT_FOUND_OR_UPDATE_FAILED + ", id=" + dishId);
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
