package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    @Select("select id, openid, name, phone, sex, id_number, avatar, create_time from `user` where openid = #{openid} limit 1")
    User getByOpenid(String openid);

    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
            insert into `user` (openid, name,create_time)
            values (#{openid}, #{name}, #{createTime})
           
            """)
    void insert(User user);
}
