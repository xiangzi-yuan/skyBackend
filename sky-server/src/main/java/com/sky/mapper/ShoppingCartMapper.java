package com.sky.mapper;

import com.sky.vo.ShoppingCartRM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    @Select("""
            select id, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, name, create_time
            from shopping_cart
            where user_id = #{userId}
            order by create_time desc
            """)
    List<ShoppingCartRM> listByUserId(Long userId);

}
