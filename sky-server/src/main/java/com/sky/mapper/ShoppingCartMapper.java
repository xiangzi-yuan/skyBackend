package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.ShoppingCartRM;
import org.apache.ibatis.annotations.*;

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

    /**
     * 根据用户ID和商品信息查询购物车项
     * 用于判断该商品是否已在购物车中
     */
    ShoppingCart getByUserAndItem(ShoppingCart shoppingCart);

    /**
     * 更新购物车项的数量
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 新增购物车项
     */
    @Insert("""
            insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time)
            values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})
            """)
    @AutoFill(OperationType.INSERT)
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clean(Long userId);
}
