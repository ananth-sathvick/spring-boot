package com.example.service;

import com.example.model.Role;

public interface RoleService {
    Role findRoleByRoleName(String name);
}