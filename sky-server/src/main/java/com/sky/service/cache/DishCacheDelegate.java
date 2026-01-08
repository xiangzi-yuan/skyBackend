package com.sky.service.cache;

import com.sky.constant.CategoryConstant;
import com.sky.constant.StatusConstant;
import com.sky.converter.dish.DishReadConvert;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.vo.dish.DishDetailVO;
import com.sky.vo.dish.DishFlavorVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DishCacheDelegate {

    private final DishMapper dishMapper;    private final DishFlavorMapper dishFlavorMapper;    private final DishReadConvert dishReadConvert;
    /**
     * 用户端：按分类查询上架菜品列表（版本号 key）。
     */
    @Cacheable(cacheNames = "dishCache", key = "#categoryId + ':v' + #ver")
    public List<DishDetailVO> listOnSaleByCategoryIdCached(Long categoryId, long ver) {
        List<DishDetailRM> dishDetailRMList = dishMapper.getByCategoryId(categoryId, StatusConstant.ENABLE,
                CategoryConstant.DISH);
        if (dishDetailRMList == null || dishDetailRMList.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为 VO 列表
        List<DishDetailVO> voList = dishDetailRMList.stream()
                .map(dishReadConvert::toDetailVO)
                .toList();

        // 批量查询所有菜品的口味
        List<Long> dishIds = voList.stream()
                .map(DishDetailVO::getId)
                .toList();

        List<DishFlavor> allFlavors = dishFlavorMapper.selectByDishIds(dishIds);

        // 按 dishId 分组
        Map<Long, List<DishFlavorVO>> flavorMap = allFlavors.stream()
                .collect(Collectors.groupingBy(
                        DishFlavor::getDishId,
                        Collectors.mapping(dishReadConvert::toFlavorVO, Collectors.toList())));

        // 设置每个菜品的口味
        voList.forEach(vo -> vo.setFlavors(flavorMap.getOrDefault(vo.getId(), Collections.emptyList())));

        return voList;
    }

    /**
     * 菜品详情：按 id 缓存。
     *
     * <p>
     * 主表不存在返回 null（由 Service 层抛业务异常）。
     * </p>
     */
    @Cacheable(cacheNames = "dishDetailCache", key = "#id")
    public DishDetailVO getDetailByIdCached(Long id) {
        DishDetailRM dishDetailRM = dishMapper.getDetailById(id);
        if (dishDetailRM == null) {
            return null;
        }

        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        DishDetailVO vo = dishReadConvert.toDetailVO(dishDetailRM);
        List<DishFlavorVO> flavorVOs = dishReadConvert.toFlavorVOList(flavors);
        vo.setFlavors(flavorVOs);
        return vo;
    }
}
