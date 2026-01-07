package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

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

        @Select("""
                        select id, openid, name, phone, sex, id_number, avatar, create_time
                        from `user`
                        where id = #{id}
                        """)
        User getById(@Param("id") Long id);

        /**
         * 统计指定时间范围内新增的用户数
         *
         * @param begin 开始时间
         * @param end   结束时间
         * @return
         */
        Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

        /**
         * 统计指定时间之前的总用户数
         *
         * @param end 结束时间
         * @return
         */
        @Select("select count(id) from `user` where create_time < #{end}")
        Integer countByCreateTimeLT(@Param("end") LocalDateTime end);

}
