package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.example.model.Expense;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
public interface ExpenseRepository extends JpaRepository<Expense, Integer>{
    @Query(value = "SELECT * FROM expense WHERE date >= ?1 AND date <= ?2 AND user_id LIKE ?3 AND category_id LIKE ?4", nativeQuery = true)
    List<Expense> getAllExpenseBw(Date d1, Date d2, String uid, String cid);

    @Query(value = "SELECT * FROM expense e WHERE e.category_id = ?1", nativeQuery = true)
	List<Expense> getByCategory(Integer cid);

    @Query(value = "SELECT * FROM expense e WHERE e.user_id = ?1", nativeQuery = true)
	List<Expense> getByUser(Integer uid);

	@Query(value = "SELECT * FROM expense WHERE user_id LIKE ?1 AND category_id LIKE ?2", nativeQuery = true)
	List<Expense> getByUserCategory(String uid, String cid);

    @Query(value = "SELECT u.*, t.amount FROM user u INNER JOIN " +
    "(SELECT user_id, SUM(amount) AS amount FROM expense " +
    "GROUP BY user_id) AS t WHERE u.id = t.user_id", nativeQuery = true)
    List<Object[]> getNetPerUser();

    @Query(value = "SELECT u.*, t.amount FROM user u INNER JOIN " +
    "(SELECT user_id, SUM(amount) AS amount FROM expense WHERE date >= ?1 AND " +
    "date <= ?2 GROUP BY user_id) AS t WHERE u.id = t.user_id", nativeQuery = true)
    List<Object[]> getNetPerUser(Date d1, Date d2);

    @Query(value = "SELECT c.*, t.amount FROM category c INNER JOIN " +
    "(SELECT category_id, SUM(amount) AS amount FROM expense GROUP BY category_id) "+
    "AS t WHERE c.id = t.category_id", nativeQuery = true)
    List<Object[]> getNetPerCategory();

    @Query(value = "SELECT c.*, t.amount FROM category c INNER JOIN " +
    "(SELECT category_id, SUM(amount) AS amount FROM expense WHERE "+
    "date >= ?1 AND date <= ?2 GROUP BY category_id) AS t " +
    " WHERE c.id = t.category_id", nativeQuery = true)
    List<Object[]> getNetPerCategory(Date d1, Date d2);

    @Query(value = "SELECT SUM(e.amount) FROM expense e WHERE user_id = ?1 AND MONTH(e.date) = MONTH(?2) AND YEAR(e.date) = YEAR(?2) ", nativeQuery = true)
    Integer getCurrentMonthExpense(String uid,Date date);

    @Query(value = "SELECT AVG(e.amount) as avg,COUNT(*) as count FROM expense e WHERE user_id = ?1 AND e.date < ?2 AND NOT MONTH(e.date) = MONTH(?2) ", nativeQuery = true)
    Map<String,Object> getPreviousMonthAverageExpense(String uid,Date date);
}
