package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.dish.DishDetailRM;
import com.sky.readmodel.dish.DishPageRM;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量（排除已删除）
     */
    @Select("select count(id) from dish where category_id = #{categoryId} and is_deleted = 0")
    Integer countByCategoryId(Long categoryId);


    /**
     * 分页查询
     */
    Page<DishPageRM> pageQuery(DishPageQueryDTO dto);

    /**
     * 根据ID查询菜品详情（含分类名称，排除已删除）
     */
    @Select(
            "select d.id, " +
                    "       d.name, " +
                    "       d.category_id, " +
                    "       c.name as category_name, " +
                    "       d.price, " +
                    "       d.image, " +
                    "       d.description, " +
                    "       d.status " +
                    "from dish d " +
                    "left join category c on d.category_id = c.id " +
                    "where d.id = #{id} and d.is_deleted = 0"
    )
    DishDetailRM getDetailById(Long id);

    /**
     * 根据分类ID查询菜品列表
     * @param categoryId 分类ID
     * @param status 状态（可选，null 表示查询所有状态）
     */
    List<DishDetailRM> getByCategoryId(@Param("categoryId") Long categoryId, @Param("status") Integer status, @Param("typeId") Integer typeId);

    /************************************************* 写 ***********************************/

    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert(
            "insert into dish " +
                    "(name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
                    "values " +
                    "(#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})"
    )
    void insert(Dish dish);

    /**
     * 软删除菜品（批量）
     */
    void softDelete(@Param("ids") List<Long> ids, @Param("deleteTime") LocalDateTime deleteTime);

    Integer countByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    @AutoFill(OperationType.UPDATE)
    void updateStatus(Dish dish);

    @AutoFill(OperationType.UPDATE)
    int update(Dish dish);


}
