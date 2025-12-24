package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.converter.DishFlavorWriteConvert;
import com.sky.converter.DishReadConvert;
import com.sky.converter.DishWriteConvert;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
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
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishWriteConvert dishWriteConvert;
    @Autowired
    private DishFlavorWriteConvert dishFlavorWriteConvert;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishReadConvert dishReadConvert;

    @Override
    @Transactional
    public void save(DishDTO dto) {
        Dish dish = dishWriteConvert.fromCreateDTO(dto);

        dish.setStatus(StatusConstant.DISABLE);

        dishMapper.insert(dish);

        // 2. 获取自增的菜品ID（@Options 配置会自动回填到 dish.id）
        Long dishId = dish.getId();

        // 3. 处理口味数据
        if (dto.getFlavors() != null && !dto.getFlavors().isEmpty()) {
            List<DishFlavor> flavors = dishFlavorWriteConvert.fromDTOList(dto.getFlavors());
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


}
