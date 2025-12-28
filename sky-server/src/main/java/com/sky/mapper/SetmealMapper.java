package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.dish.SetmealDetailRM;
import com.sky.readmodel.dish.SetmealPageRM;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id") // 回传套餐id
    @Insert("""
            insert into setmeal (category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) 
            values (#{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
            """)
    void insert(Setmeal setmeal);

    @AutoFill(OperationType.UPDATE)
    int update(Setmeal setmeal);

    @Select("""
            select
              s.id          as id,
              s.category_id as categoryId,
              c.name        as categoryName,
              s.name        as name,
              s.price       as price,
              s.status      as status,
              s.description as description,
              s.image       as image
            from setmeal s
            left join category c on s.category_id = c.id
            where s.id = #{id}
            """)
    SetmealDetailRM getDetailById(Long id);


    Page<SetmealPageRM> pageQuery(SetmealPageQueryDTO dto);

    @AutoFill(OperationType.UPDATE)
    void updateStatus(Setmeal setmeal);
}
