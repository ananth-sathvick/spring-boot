package com.example.controller;

import java.util.Optional;

import com.example.model.Category;
import com.example.model.Expense;
import com.example.repository.ExpenseRepository;
import com.example.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
               // by Spring, we will use it to handle the data
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // private Category cat1 = new Category("category1");

    @GetMapping(path = "/all")
    public ResponseEntity<Iterable<Expense>> getAllExpenses() {
        return new ResponseEntity<>(expenseRepository.findAll(), HttpStatus.OK);
    }

    // @GetMapping(path = "/find/{d1}/{d2}")
    // public ResponseEntity<Iterable<Expense>> getAllExpenseBw() {
    //     return new ResponseEntity<>(expenseRepository.findAll(), HttpStatus.OK);
    // }

    @GetMapping("/find/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable("id") Integer id) {
        Optional<Expense> expense = expenseRepository.findById(id);
        if(expense.isPresent())
            return new ResponseEntity<>(expense.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
	}
	
	@PostMapping("/add")
	public ResponseEntity<Expense> addExpense(@RequestBody Expense expense){
        Category tcat = categoryRepository.findById(1).get();
        tcat.getExpenseList().add(expense);
        expense.setCategory(tcat);
        categoryRepository.save(tcat);
		Expense newExpense = expenseRepository.save(expense);
		return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
	}
	
	@PutMapping("/update")
	public ResponseEntity<Expense> updateExpense(@RequestBody Expense expense){
		Expense updateExpense = expenseRepository.save(expense);
		return new ResponseEntity<>(updateExpense, HttpStatus.OK);
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteExpense(@PathVariable("id") Integer id){
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
		return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
	}
}
