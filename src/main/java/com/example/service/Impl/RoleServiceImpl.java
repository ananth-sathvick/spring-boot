package com.example.service.Impl;

import com.example.model.Role;
import com.example.repository.RoleRepository;
import com.example.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "roleService")
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleDao;

    @Override
    public Role findRoleByRoleName(String name) {
        Role role = roleDao.findRoleByRoleName(name);
        return role;
    }
}
