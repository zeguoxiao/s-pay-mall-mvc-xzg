package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.domain.po.User;

@Mapper
public interface IUserDao {
    User queryByUserName(@Param("userName") String userName);
    User queryById(@Param("id") Long id);
    int insert(User user);
}