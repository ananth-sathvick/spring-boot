package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

import com.example.model.Expense;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
public interface ExpenseRepository extends JpaRepository<Expense, Integer>{
    @Query("SELECT e FROM Expense e WHERE e.date >= ?1 and e.date <= ?2")
    List<Expense> getAllExpenseBw(Date d1, Date d2);

    @Query(value = "SELECT * FROM expense e WHERE e.category_id = ?1", nativeQuery = true)
	List<Expense> getByCategory(Integer cid);

    @Query(value = "SELECT * FROM expense e WHERE e.user_id = ?1", nativeQuery = true)
	List<Expense> getByUser(Integer uid);

	@Query(value = "SELECT * FROM expense e WHERE e.user_id = ?1 and e.category_id = ?2", nativeQuery = true)
	List<Expense> getByUserCategory(Integer uid, Integer cid);

    @Query(value = "SELECT u.*, t.amount FROM user u INNER JOIN (SELECT user_id, SUM(amount) AS amount FROM expense GROUP BY user_id) AS t WHERE u.id = t.user_id", nativeQuery = true)
    List<Object[]> getNetPerUser();
}
