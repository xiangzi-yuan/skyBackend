package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.readmodel.dish.DishPageRM;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
    insert into dish
    (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
    values
    (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
    """)
    void insert(Dish dish);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    Page<DishPageRM> pageQuery(DishPageQueryDTO dto);
    
    /**
     * 根据ID查询菜品详情（含分类名称）
     * @param id 菜品ID
     * @return 菜品详情 ReadModel（不含口味，口味单独查询）
     */
    @Select("""
        select d.id,
               d.name,
               d.category_id,
               c.name as category_name,
               d.price,
               d.image,
               d.description,
               d.status,
               d.update_time
        from dish d
        left join category c on d.category_id = c.id
        where d.id = #{id}
    """)
    DishDetailRM getDetailById(Long id);
}
