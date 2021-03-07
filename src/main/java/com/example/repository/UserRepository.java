package com.example.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.example.model.User;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByEmail(String username);

    @Query(value = "SELECT * FROM user u WHERE u.role_id = ?1", nativeQuery = true)
	Iterable<User> getAll(Integer role_id);
}