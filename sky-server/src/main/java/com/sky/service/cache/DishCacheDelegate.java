package com.sky.service.cache;

import com.sky.constant.CategoryConstant;
import com.sky.constant.StatusConstant;
import com.sky.converter.DishReadConvert;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.vo.dish.DishDetailVO;
import com.sky.vo.dish.DishFlavorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishCacheDelegate {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishReadConvert dishReadConvert;

    /**
     * 用户端：按分类查询上架菜品列表（版本号 key）。
     */
    @Cacheable(cacheNames = "dishCache", key = "#categoryId + ':v' + #ver")
    public List<DishDetailVO> listOnSaleByCategoryIdCached(Long categoryId, long ver) {
        List<DishDetailRM> dishDetailRMList = dishMapper.getByCategoryId(categoryId, StatusConstant.ENABLE, CategoryConstant.DISH);
        return dishDetailRMList.stream()
                .map(dishReadConvert::toDetailVO)
                .toList();
    }

    /**
     * 菜品详情：按 id 缓存。
     *
     * <p>主表不存在返回 null（由 Service 层抛业务异常）。</p>
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

