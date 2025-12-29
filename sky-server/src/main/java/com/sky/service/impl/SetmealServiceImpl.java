package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.converter.SetmealReadConvert;
import com.sky.converter.SetmealWriteConvert;
import com.sky.dto.setmeal.SetmealCreateDTO;
import com.sky.dto.setmeal.SetmealPageQueryDTO;
import com.sky.dto.setmeal.SetmealUpdateDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.readmodel.setmeal.SetmealDetailRM;
import com.sky.readmodel.setmeal.SetmealPageRM;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.setmeal.SetmealDetailVO;
import com.sky.vo.setmeal.SetmealDishVO;
import com.sky.vo.setmeal.SetmealPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealWriteConvert setmealWriteConvert;
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    SetmealReadConvert setmealReadConvert;

    @Override
    public void save(SetmealCreateDTO dto) {
        Setmeal setmeal = setmealWriteConvert.fromCreateDTO(dto);
        setmeal.setStatus(StatusConstant.DISABLE); // 设置忽略的状态变量,初始为停售

        setmealMapper.insert(setmeal);

        // setmeal_dish表
        Long setmealId = setmeal.getId(); // mapper要返回
        if (dto.getSetmealDishes() != null && !dto.getSetmealDishes().isEmpty()) {
            List<SetmealDish> setmealDishes = setmealWriteConvert.fromDishDTOList(dto.getSetmealDishes());
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));

            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<SetmealPageRM> page = setmealMapper.pageQuery(dto);
        long total = page.getTotal();
        List<SetmealPageVO> records = page.getResult()
                .stream()
                .map(setmealReadConvert::toPageVO)
                .collect(Collectors.toList());

        return new PageResult(total, records);
    }
    /**
     * 修改菜品：UpdateDTO -> Entity（局部更新）
     */
    @Override
    public void changeSetmeal(SetmealUpdateDTO dto) {
        Long setmealId = dto.getId();
        if (setmealId == null) {
            throw new IllegalArgumentException("setmealId 不能为空");
        }
        Setmeal setmeal = new Setmeal();
        setmealWriteConvert.mergeUpdate(dto, setmeal);
        setmeal.setId(setmealId); // !!!不要忘了mergeUpdate忽略了id
        int rows = setmealMapper.update(setmeal);

        if (rows != 1) {
            throw new RuntimeException("套餐不存在或更新失败, id=" + setmealId);
        }
        // 2) 子表：先删（未传需要手动删除,所以认为不传表示清空）
        setmealDishMapper.deleteBySetmealId(setmealId);

        // 3) 有菜品则插入；无/空则保持清空
        if (dto.getSetmealDishes() == null || dto.getSetmealDishes().isEmpty()) {
            return;
        }
        List<SetmealDish> dishes = setmealWriteConvert.fromDishDTOList(dto.getSetmealDishes());
        dishes.forEach(f -> f.setSetmealId(setmealId));

        if (!dishes.isEmpty()) {
            setmealDishMapper.insertBatch(dishes);
        }
    }


    /**
     * 根据ID查询套餐详情
     *
     * <p>查询流程：
     * <ol>
     *   <li>查询套餐基础信息（包含分类名称）</li>
     *   <li>查询菜套餐关联的口味列表</li>
     *   <li>组装并转换为 VO 返回</li>
     * </ol>
     *
     * @param id 套餐ID
     * @return 套餐详情 VO（包含基础信息和口味列表）
     */
    @Override
    public SetmealDetailVO getDetailById(Long id) {
        SetmealDetailRM setmealDetailRM = setmealMapper.getDetailById(id);
        if (setmealDetailRM == null) {
            throw new RuntimeException("套餐不存在，id=" + id);
        }
        // 查询关联菜品列表
        List<SetmealDish> dishes = setmealDishMapper.selectBySetmealId(id);

        SetmealDetailVO vo = setmealReadConvert.toDetailVO(setmealDetailRM);
        List<SetmealDishVO> dishVOS = setmealReadConvert.toDishVOList(dishes);
        vo.setSetmealDishes(dishVOS);
        return vo;
    }


    /**
     * 删除套餐（支持批量）- 软删除
     *
     * <p>业务规则：
     * <ul>
     *   <li>起售中的套餐不能删除</li>
     *   <li>采用软删除，保留历史数据用于订单统计</li>
     *   <li>套餐-菜品关联表不删除，因为订单详情可能需要展示</li>
     * </ul>
     *
     * @param ids 套餐ID列表
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 1. 校验：起售中的套餐不能删除
        List<Long> idList = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (idList.isEmpty()) {
            return;
        }

        // 去数据库里统计——在要删除的这批 id 里，有多少条套餐的 status = ENABLE
        Integer onSaleCount = setmealMapper.countByIdsAndStatus(idList, StatusConstant.ENABLE);
        if (onSaleCount != null && onSaleCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        // 2. 软删除：标记 is_deleted = 1，保留数据用于历史订单查询
        // 注意：setmeal_dish 关联表数据不删除，因为订单详情可能需要展示套餐内容
        setmealMapper.softDelete(idList, LocalDateTime.now());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.updateStatus(setmeal);

    }

}
