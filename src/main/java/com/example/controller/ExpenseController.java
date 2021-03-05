package com.example.controller;

import java.sql.Date;
import java.util.Optional;

import com.example.model.Category;
import com.example.model.Expense;
import com.example.repository.ExpenseRepository;
import com.example.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/expense") // This means URL's start with /demo (after Application path)
public class ExpenseController {
    @Autowired // This means to get the bean called userRepository // Which is auto-generated
    private ExpenseRepository expenseRepository; // by Spring, we will use it to handle the data

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping(path = "/all") // returns a list of all the expenses without any filter
    public ResponseEntity<Iterable<Expense>> getAllExpenses() {
        return new ResponseEntity<>(expenseRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(path = "/findBw/{d1}/{d2}") // returns a list of expenses logged between Date(d1) and Date(d2) (d1 < d2)
    public ResponseEntity<Iterable<Expense>> getAllExpenseBw(@PathVariable("d1") Date d1, @PathVariable("d2") Date d2 ) {
        return new ResponseEntity<>(expenseRepository.getAllExpenseBw(d1, d2), HttpStatus.OK);
    }

    @GetMapping(path = "/findByCategory/{cid}") // returns a list of expenses logged under category with (category_id = cid)
    public ResponseEntity<Iterable<Expense>> getByCategory(@PathVariable("cid") Integer cid) {
        return new ResponseEntity<>(expenseRepository.getByCategory(cid), HttpStatus.OK);
    }

    @GetMapping(path = "/findByUser/{uid}") // returns a list of expenses logged under user with (user_id = uid)
    public ResponseEntity<Iterable<Expense>> getByUser(@PathVariable("uid") Integer uid) {
        return new ResponseEntity<>(expenseRepository.getByUser(uid), HttpStatus.OK);
    }

    // returns a list of expenses logged under user with (user_id = cid) and category with (category_id = cid)
    @GetMapping(path = "/findByUserCategory/{uid}/{cid}") 
    public ResponseEntity<Iterable<Expense>> getByUser(@PathVariable("uid") Integer uid, @PathVariable("uid") Integer cid) {
        return new ResponseEntity<>(expenseRepository.getByUserCategory(uid, cid), HttpStatus.OK);
    }

    @GetMapping(path = "/find/{id}") // returns the expense with expense_id = id
    public ResponseEntity<Expense> getExpenseById(@PathVariable("id") Integer id) {
        Optional<Expense> expense = expenseRepository.findById(id);
        if(expense.isPresent())
            return new ResponseEntity<>(expense.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
	}
	
	@PostMapping(path = "/add/{cid}") // creates a new expense and return the new expense entity
	public ResponseEntity<Expense> addExpense(@RequestBody Expense expense, @PathVariable("cid") Integer cid){
        if(categoryRepository.existsById(cid)){
            Category tcat = categoryRepository.findById(cid).get();
            tcat.getExpenseList().add(expense);
            expense.setCategory(tcat);
            categoryRepository.save(tcat);
            Expense newExpense = expenseRepository.save(expense);
            return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(null, HttpStatus.CONFLICT);
	}
	
	@PutMapping(path = "/update") // updates an existing expense with new the values and returns the updated expense entity
	public ResponseEntity<Expense> updateExpense(@RequestBody Expense expense){
		Expense updateExpense = expenseRepository.save(expense);
		return new ResponseEntity<>(updateExpense, HttpStatus.OK);
	}
	
	@DeleteMapping(path = "/delete/{id}") // deletes the expense with expense_id = id 
	public ResponseEntity<?> deleteExpense(@PathVariable("id") Integer id){
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
		return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
	}
}
