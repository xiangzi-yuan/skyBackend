package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.category.CategoryDetailRM;
import com.sky.readmodel.category.CategoryPageRM;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper {

    /**
     * 插入数据
     * @param category 分类实体
     */
    @AutoFill(OperationType.INSERT)
    @Insert("INSERT INTO category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "VALUES (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /**
     * 分页查询 - 返回 PageRM
     * @param dto 分页查询条件
     * @return 分页结果
     */
    Page<CategoryPageRM> pageQuery(CategoryPageQueryDTO dto);

    /**
     * 根据ID查询详情 - 返回 DetailRM
     * @param id 分类ID
     * @return 分类详情
     */
    @Select("SELECT id, name, type, sort, status FROM category WHERE id = #{id} AND is_deleted = 0")
    CategoryDetailRM getById(Long id);

    /**
     * 根据ID查询实体（用于更新前查询）
     * @param id 分类ID
     * @return 分类实体
     */
    @Select("SELECT * FROM category WHERE id = #{id} AND is_deleted = 0")
    Category getEntityById(Long id);

    /**
     * 根据ID软删除分类
     * @param id 分类ID
     * @param deleteTime 删除时间
     */
    @Update("UPDATE category SET is_deleted = 1, delete_time = #{deleteTime},update_time = #{deleteTime} WHERE id = #{id}")
    void softDeleteById(@Param("id") Long id, @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 根据ID修改分类
     * @param category 分类实体
     */
    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    /**
     * 根据类型查询分类（用于下拉选择）
     * @param type 分类类型（1-菜品分类，2-套餐分类，null-全部）
     * @return 分类列表
     */
    List<Category> list(@Param("type") Integer type);
}
