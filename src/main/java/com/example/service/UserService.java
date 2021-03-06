package com.example.service;

import java.util.List;

import com.example.model.User;

public interface UserService {
    List<User> findAll();
    User findOne(String username);
}
