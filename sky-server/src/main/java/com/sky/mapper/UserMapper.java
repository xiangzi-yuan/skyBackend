package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

        /**
         * 按日期分组统计新增用户数（用于批量报表导出）
         *
         * @param begin 开始时间
         * @param end   结束时间
         * @return 每日新用户数列表，每个Map包含 date 和 count
         */
        List<Map<String, Object>> countGroupByCreateDate(
                @Param("begin") LocalDateTime begin,
                @Param("end") LocalDateTime end
        );


}
