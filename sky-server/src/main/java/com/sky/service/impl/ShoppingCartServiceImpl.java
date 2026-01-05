package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.readmodel.dish.DishItemRM;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartRM;
import com.sky.vo.ShoppingCartVO;
import com.sky.vo.dish.DishItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Override
    public List<ShoppingCartVO> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCartRM> rmList = shoppingCartMapper.listByUserId(userId); // 先按你现在的写法
        return rmList.stream()
                .map(rm -> {
                    ShoppingCartVO vo = new ShoppingCartVO();
                    BeanUtils.copyProperties(rm, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }


}
