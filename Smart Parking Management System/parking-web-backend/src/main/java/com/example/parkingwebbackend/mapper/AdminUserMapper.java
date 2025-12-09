package com.example.parkingwebbackend.mapper;

import com.example.parking.model.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper {
    AdminUser getUserByUsername(String username);
    int insertUser(AdminUser user);
    int updateUser(AdminUser user);
    int deleteUser(Integer userId);
}