package com.example.service;

import java.util.List;

import com.example.model.User;
import com.example.model.UserDto;

public interface UserService {
    User save(UserDto user);
    List<User> findAll();
    User findOne(String username);
}
