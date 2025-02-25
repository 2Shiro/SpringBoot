package com.tenco.class_jwt.mapper;

import com.tenco.class_jwt.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void save(User user);
    User findByUsername(String username);
    void updateRefreshToken(User user);
}
