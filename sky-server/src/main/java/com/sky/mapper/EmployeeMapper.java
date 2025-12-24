package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.readmodel.employee.EmployeeAuthInfo;
import com.sky.readmodel.employee.EmployeeDetailRM;
import com.sky.readmodel.employee.EmployeeLoginRM;
import com.sky.readmodel.employee.EmployeePageRM;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface EmployeeMapper {
    /*************************************** 读 *************************************/
    /**
     * 登录用：只查登录必要字段（禁止 select *）
     */
    @Select("select " +
            "id, username, name, password, status, role, pwd_changed as pwdChanged " +
            "from employee " +
            "where username = #{username}")
    EmployeeLoginRM getLoginInfoByUsername(String username);

    /**
     * 详情展示：返回 RM，不返回 VO
     */
    @Select("select " +
            "id, name, username, phone, sex, " +
            "id_number as idNumber, " +
            "status, create_time as createTime, update_time as updateTime " +
            "from employee " +
            "where id = #{id}")
    EmployeeDetailRM getDetailById(Long id);

    /**
     * 业务鉴权/修改密码：最小必要字段
     */
    @Select("select " +
            "id, password, status, role, pwd_changed as pwdChanged " +
            "from employee " +
            "where id = #{id}")
    EmployeeAuthInfo getAuthInfoById(Long id);

    /**
     * 分页查询：返回 RM（SQL 在 XML）
     */
    Page<EmployeePageRM> pageQuery(EmployeePageQueryDTO dto);

    /*************************************** 写 *************************************/
    /**
     * 新增：写操作仍然使用 Entity
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into employee " +
            "(name, username, password, phone, sex, id_number, status, pwd_changed, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{pwdChanged}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);



    @AutoFill(OperationType.UPDATE)
    @Update("update employee " +
            "set password = #{password}, " +
            "    pwd_changed = #{pwdChanged}, " +
            "    update_time = #{updateTime}, " +
            "    update_user = #{updateUser} " +
            "where id = #{id}")
    void updatePasswordAndMarkChanged(Employee employee);




    @AutoFill(OperationType.UPDATE)
    void update(Employee emp);

    @AutoFill(OperationType.UPDATE)
    void updateStatus(Employee emp);
}
