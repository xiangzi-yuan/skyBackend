package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.setmeal.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.setmeal.SetmealDetailRM;
import com.sky.readmodel.setmeal.SetmealListRM;
import com.sky.readmodel.setmeal.SetmealPageRM;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量（排除已删除）
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId} and is_deleted = 0")
    Integer countByCategoryId(Long id);


    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id") // 回传套餐id
    @Insert("""
            insert into setmeal (category_id, name, price, status, description,
                                 image, create_time, update_time, create_user, update_user)
            values (#{categoryId}, #{name}, #{price}, #{status}, #{description},
                    #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
            """)
    void insert(Setmeal setmeal);

    @AutoFill(OperationType.UPDATE)
    int update(Setmeal setmeal);

    @Select("""
            select
              s.id          as id,
              s.category_id as categoryId,
              s.name        as name,
              s.price       as price,
              s.description as description,
              s.image       as image,
              s.status      as status
            from setmeal s
            left join category c on s.category_id = c.id
            where s.id = #{id} and s.is_deleted = 0
            """)
    SetmealDetailRM getDetailById(Long id);


    Page<SetmealPageRM> pageQuery(SetmealPageQueryDTO dto);

    @AutoFill(OperationType.UPDATE)
    void updateStatus(Setmeal setmeal);

    /**
     * 软删除套餐（批量）
     */
    void softDelete(@Param("ids") List<Long> ids, @Param("deleteTime") LocalDateTime deleteTime);

    Integer countByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据分类ID查询套餐列表（可选按状态过滤，User端专用）
     */
    List<SetmealListRM> listByCategoryId(@Param("categoryId") Long categoryId, @Param("status") Integer status);
}
