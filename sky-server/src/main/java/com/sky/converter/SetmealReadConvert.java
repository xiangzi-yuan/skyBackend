package com.sky.converter;

import com.sky.entity.SetmealDish;
import com.sky.readmodel.setmeal.SetmealDetailRM;
import com.sky.readmodel.setmeal.SetmealListRM;
import com.sky.readmodel.setmeal.SetmealPageRM;
import com.sky.vo.setmeal.SetmealDetailVO;
import com.sky.vo.setmeal.SetmealDishVO;
import com.sky.vo.setmeal.SetmealListVO;
import com.sky.vo.setmeal.SetmealPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * ：VO 已经把输出字段限制成“可公开的白名单”，
 * 所以读转换通常不需要 ignore；
 * 敏感控制不是靠转换器，而是靠 VO 的字段设计与接口返回类型。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SetmealReadConvert {
    SetmealDetailVO toDetailVO(SetmealDetailRM setmealDetailRM);

    List<SetmealDishVO> toDishVOList(List<SetmealDish> dishes);

    SetmealPageVO toPageVO(SetmealPageRM setmealPageRM);

    SetmealListVO toListVO(SetmealListRM setmealListRM);

    List<SetmealListVO> toListVOList(List<SetmealListRM> setmealListRMList);
}
