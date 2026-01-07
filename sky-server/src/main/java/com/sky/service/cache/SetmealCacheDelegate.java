package com.sky.service.cache;

import com.sky.constant.StatusConstant;
import com.sky.converter.SetmealReadConvert;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.readmodel.setmeal.SetmealDetailRM;
import com.sky.vo.setmeal.SetmealDetailVO;
import com.sky.vo.setmeal.SetmealDishVO;
import com.sky.vo.setmeal.SetmealListVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetmealCacheDelegate {

    private final SetmealMapper setmealMapper;    private final SetmealDishMapper setmealDishMapper;    private final SetmealReadConvert setmealReadConvert;
    /**
     * 用户端：按分类查询上架套餐列表（版本号 key）。
     */
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId + ':v' + #ver")
    public List<SetmealListVO> listOnSaleByCategoryIdCached(Long categoryId, long ver) {
        return setmealReadConvert.toListVOList(
                setmealMapper.listByCategoryId(categoryId, StatusConstant.ENABLE)
        );
    }

    /**
     * 套餐详情：按 id 缓存。
     *
     * <p>主表不存在返回 null（由 Service 层抛业务异常）。</p>
     */
    @Cacheable(cacheNames = "setmealDetailCache", key = "#id")
    public SetmealDetailVO getDetailByIdCached(Long id) {
        SetmealDetailRM setmealDetailRM = setmealMapper.getDetailById(id);
        if (setmealDetailRM == null) {
            return null;
        }

        List<SetmealDish> dishes = setmealDishMapper.selectBySetmealId(id);
        SetmealDetailVO vo = setmealReadConvert.toDetailVO(setmealDetailRM);
        List<SetmealDishVO> dishVOS = setmealReadConvert.toDishVOList(dishes);
        vo.setSetmealDishes(dishVOS);
        return vo;
    }
}

