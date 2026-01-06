package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.vo.ShoppingCartVO;

import java.util.List;

public interface ShoppingCartService {

    List<ShoppingCartVO> list();

    void add(ShoppingCartDTO dto);

    void sub(ShoppingCartDTO dto);

    void clean();
}
