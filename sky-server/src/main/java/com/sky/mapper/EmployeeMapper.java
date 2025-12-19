package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
    /**
     * 新增员工
     * @param employee
     * @return
     */
    @Insert("insert into employee " +
            "(name, username, password, phone, sex, id_number, status, pwd_changed, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{pwdChanged}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    @Select("select pwd_changed from employee where id = #{id}")
    Integer getPwdChangedById(@Param("id") Long id);

    // sky-server: com.sky.mapper.EmployeeMapper
    @Update("update employee " +
            "set password = #{password}, pwd_changed = 1, update_time = #{updateTime}, update_user = #{updateUser} " +
            "where id = #{id}")
    void updatePasswordAndMarkChanged(@Param("id") Long id,
                                      @Param("password") String password,
                                      @Param("updateTime") LocalDateTime updateTime,
                                      @Param("updateUser") Long updateUser);

}
