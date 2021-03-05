package com.example.repository;

import com.example.model.Role;

import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Integer> {
    Role findRoleByRoleName(String name);
}