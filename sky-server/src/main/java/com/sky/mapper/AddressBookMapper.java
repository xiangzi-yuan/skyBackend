package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地址簿数据访问接口
 * 
 * <p>
 * 安全规范：所有写操作必须包含 userId 条件防止越权
 */
@Mapper
public interface AddressBookMapper {

    /**
     * 新增地址
     */
    void insert(AddressBook addressBook);

    /**
     * 查询用户所有地址
     */
    List<AddressBook> listByUserId(@Param("userId") Long userId);

    /**
     * 根据 id 和 userId 查询地址
     * <p>
     * 防止越权访问
     */
    AddressBook getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询用户默认地址
     */
    AddressBook getDefaultByUserId(@Param("userId") Long userId);

    /**
     * 更新地址（必须包含 userId 条件）
     * 
     * @return 受影响行数
     */
    int updateByIdAndUserId(AddressBook addressBook);

    /**
     * 根据 id 和 userId 删除地址
     * 
     * @return 受影响行数
     */
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 清除用户所有默认地址标记
     * 
     * @return 受影响行数
     */
    int clearDefaultByUserId(@Param("userId") Long userId);

    /**
     * 设置指定地址为默认
     * 
     * @return 受影响行数（0 表示地址不存在或不属于该用户）
     */
    int setDefaultByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
