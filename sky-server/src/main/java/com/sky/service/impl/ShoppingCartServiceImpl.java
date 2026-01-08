package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.readmodel.ShoppingCartRM;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.readmodel.setmeal.SetmealDetailRM;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.sky.constant.MessageConstant.DISH_ID_AND_SETMEAL_ID_CANNOT_BOTH_BE_NULL;
import static com.sky.constant.MessageConstant.SHOPPING_CART_ITEM_NOT_FOUND;
import lombok.RequiredArgsConstructor;


@Service
@Slf4j
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public List<ShoppingCartVO> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCartRM> rmList = shoppingCartMapper.listByUserId(userId);
        return rmList.stream()
                .map(rm -> {
                    ShoppingCartVO vo = new ShoppingCartVO();
                    BeanUtils.copyProperties(rm, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 添加购物车
     * 业务逻辑：
     * 1. 判断当前请求的商品（菜品+口味 或 套餐）是否已在购物车中
     * 2. 如果已存在，数量+1
     * 3. 如果不存在，新增一条购物车记录
     */
    @Override
    public void add(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 构建查询条件
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(dto.getDishId())
                .setmealId(dto.getSetmealId())
                .dishFlavor(dto.getDishFlavor())
                .build();

        // 1. 查询当前商品是否已在购物车中
        ShoppingCart existingCart = shoppingCartMapper.getByUserAndItem(shoppingCart);

        if (existingCart != null) {
            // 2. 已存在：数量+1
            existingCart.setNumber(existingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(existingCart);
            log.info("购物车商品数量+1，当前数量：{}", existingCart.getNumber());
        } else {
            // 3. 不存在：新增购物车记录
            shoppingCart.setNumber(1);
            // shoppingCart.setCreateTime(LocalDateTime.now());

            if (dto.getDishId() != null) {
                // 添加的是菜品
                DishDetailRM dish = dishMapper.getDetailById(dto.getDishId());
                if (dish == null) {
                    throw new ShoppingCartBusinessException(SHOPPING_CART_ITEM_NOT_FOUND);
                }
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                log.info("添加菜品到购物车：{}", dish.getName());
            } else if (dto.getSetmealId() != null) {
                // 添加的是套餐
                SetmealDetailRM setmeal = setmealMapper.getDetailById(dto.getSetmealId());
                if (setmeal == null) {
                    throw new ShoppingCartBusinessException(SHOPPING_CART_ITEM_NOT_FOUND);
                }
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
                log.info("添加套餐到购物车：{}", setmeal.getName());
            } else {
                throw new ShoppingCartBusinessException(DISH_ID_AND_SETMEAL_ID_CANNOT_BOTH_BE_NULL);
            }

            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public void sub(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(dto.getDishId())
                .setmealId(dto.getSetmealId())
                .dishFlavor(dto.getDishFlavor())
                .build();
        ShoppingCart existingCart = shoppingCartMapper.getByUserAndItem(shoppingCart);
        if (existingCart == null) {
            // 不存在
            throw new ShoppingCartBusinessException(SHOPPING_CART_ITEM_NOT_FOUND);
        } else {
            if (existingCart.getNumber() <= 1) {
                shoppingCartMapper.deleteByIdAndUserId(existingCart.getId(), userId);
            } else {
                existingCart.setNumber(existingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(existingCart);
                log.info("购物车商品数量-1，当前数量：{}", existingCart.getNumber());
            }

        }
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }

}
