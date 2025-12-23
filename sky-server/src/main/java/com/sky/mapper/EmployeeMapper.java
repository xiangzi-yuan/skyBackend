package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.auth.model.EmployeeAuthInfo;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.vo.EmployeeDetailVO;
import com.sky.vo.EmployeePageVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    @Select("select id, name, username, phone, sex, id_number, status, create_time, update_time from employee where id = #{id}")
    EmployeeDetailVO getDetailById(Long id);
    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @Insert("insert into employee " +
            "(name, username, password, phone, sex, id_number, status, pwd_changed, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{pwdChanged}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    @Select("select id, password, status, role, pwd_changed from employee where id = #{id}")
    EmployeeAuthInfo getAuthInfoById(Long id);

    // sky-server: com.sky.mapper.EmployeeMapper
    @Update("update employee " +
            "set password = #{password}, pwd_changed = 1, update_time = #{updateTime}, update_user = #{updateUser} " +
            "where id = #{id}")
    void updatePasswordAndMarkChanged(@Param("id") Long id,
                                      @Param("password") String password,
                                      @Param("updateTime") LocalDateTime updateTime,
                                      @Param("updateUser") Long updateUser);

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    Page<EmployeePageVO> pageQuery(EmployeePageQueryDTO dto);


    /**
     * 员工信息修改
     *
     * @param emp
     */
    void update(Employee emp);

    /**
     * 修改状态
     *
     * @param emp
     */
    void updateStatus(Employee emp);



}
