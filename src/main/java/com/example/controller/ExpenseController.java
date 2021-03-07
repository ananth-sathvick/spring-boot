package com.example.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.example.model.Category;
import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.ExpenseRepository;
import com.example.repository.UserRepository;
import com.example.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/all") // returns a list of all the expenses without any filter
    public ResponseEntity<Iterable<Expense>> getAllExpenses() {
        return new ResponseEntity<>(expenseRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(path = "/my")
    public ResponseEntity<Iterable<Expense>> getAllUserExpenses() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        Integer uid = userRepository.findByEmail(email).getId();
        return new ResponseEntity<>(expenseRepository.getByUser(uid), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/findByUser/{uid}") // returns a list of expenses logged under user with (user_id = uid)
    public ResponseEntity<Iterable<Expense>> findByUser(@PathVariable("uid") Integer uid) {
        return new ResponseEntity<>(expenseRepository.getByUser(uid), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/find/{id}") // returns the expense with expense_id = id
    public ResponseEntity<Expense> findExpenseById(@PathVariable("id") Integer id) {
        Optional<Expense> expense = expenseRepository.findById(id);
        if (expense.isPresent()) {
            System.out.println(expense.get().getClass());
            return new ResponseEntity<>(expense.get(), HttpStatus.OK);
        } else
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole('ADMIN')") // returns a list of expenses logged under user with (user_id = cid) and category with (category_id = cid)
    @GetMapping(path = "/findBy/{uid}/{cid}") 
    public ResponseEntity<Iterable<Expense>> findByUser(@PathVariable("uid") String uid, @PathVariable("uid") String cid) {
        if(uid.equals("*"))
            uid = "%";

        if(cid.equals("*"))
            cid = "%";
        return new ResponseEntity<>(expenseRepository.getByUserCategory(uid, cid), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/findBy/{uid}/{cid}/{d1}/{d2}") // returns a list of expenses logged between Date(d1) and Date(d2) (d1 < d2)
    public ResponseEntity<Iterable<Expense>> getAllExpenseBw(@PathVariable("uid") String uid, @PathVariable("cid") String cid,
        @PathVariable("d1") Date d1, @PathVariable("d2") Date d2) {
        if(uid.equals("*"))
            uid = "%";

        if(cid.equals("*"))
            cid = "%";
        return new ResponseEntity<>(expenseRepository.getAllExpenseBw(d1, d2, uid, cid), HttpStatus.OK);
    }

    @GetMapping(path = "/findBy/{cid}") // returns a list of expenses logged under category with (category_id = cid)
    public ResponseEntity<Iterable<Expense>> findByCategory(@PathVariable("cid") String cid) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        return new ResponseEntity<>(expenseRepository.getByUserCategory(
            String.valueOf(user.getId()), cid), HttpStatus.OK
        );
    }

    @GetMapping(path = "/findBy/{cid}/{d1}/{d2}") // returns a list of expenses logged between Date(d1) and Date(d2) (d1 < d2)
    public ResponseEntity<Iterable<Expense>> getAllExpenseBw(@PathVariable("cid") String cid, @PathVariable("d1") Date d1, 
        @PathVariable("d2") Date d2) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        if(cid.equals("*"))
            cid = "%";
        return new ResponseEntity<>(expenseRepository.getAllExpenseBw(d1, d2, String.valueOf(user.getId()), cid), HttpStatus.OK);
    }

	// creates a new expense under user with (user_id = cid) and category with (category_id = cid) and returns the new expense
    @PreAuthorize("hasRole('ADMIN')")
	@PostMapping(path = "/add/{uid}/{cid}")
	public ResponseEntity<Expense> addExpense(@RequestBody Expense expense, @PathVariable("uid") Integer uid, @PathVariable("cid") Integer cid){
        if(userRepository.existsById(uid) && categoryRepository.existsById(cid)){
            Category category = categoryRepository.findById(cid).get();
            User user = userRepository.findById(uid).get();
            category.getExpenseList().add(expense);
            user.getExpenseList().add(expense);
            expense.setCategory(category);
            expense.setUser(user);
            categoryRepository.save(category);
            userRepository.save(user);
            Expense newExpense = expenseRepository.save(expense);
            return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
        } else
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
	}

    @PostMapping(path = "/add/{cid}")
	public ResponseEntity<Expense> addExpense(@RequestBody Expense expense, @PathVariable("cid") Integer cid){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        if(categoryRepository.existsById(cid)){
            Category category = categoryRepository.findById(cid).get();
            category.getExpenseList().add(expense);
            user.getExpenseList().add(expense);
            expense.setCategory(category);
            expense.setUser(user);
            categoryRepository.save(category);
            userRepository.save(user);
            Expense newExpense = expenseRepository.save(expense);
            return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
        }
        else
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
	}


	@PutMapping(path = "/update") // updates an existing expense with new the values and returns the updated expense entity
	public ResponseEntity<Expense> updateExpense(@RequestBody Expense expense){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        if(!user.getRole().getRoleName().equals("ADMIN")){
            if(expense.getId() == null || !user.getId().equals(expense.getUser().getId()))
                return new ResponseEntity<>(null, HttpStatus.NOT_MODIFIED);
        }
        if(expense.getId() != null){
            Expense updateExpense = expenseRepository.save(expense);
            return new ResponseEntity<>(updateExpense, HttpStatus.OK);
        } else
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}
	
	@DeleteMapping(path = "/delete/{id}") // deletes the expense with expense_id = id 
	public ResponseEntity<?> deleteExpense(@PathVariable("id") Integer id){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        Optional<Expense> oexpense = expenseRepository.findById(id);
        if(oexpense.isPresent()){
            if(user.getRole().getRoleName().equals("ADMIN") || user.getId().equals(oexpense.get().getUser().getId())){
                expenseRepository.deleteById(id);
                return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
             }
        }
        return new ResponseEntity<>("Error Deleting", HttpStatus.CONFLICT);
	}

    public Iterable<HashMap<String, String>> mapUserData(List<Object[]> queryResult){
        HashMap<String, String> map = new HashMap<String, String>();
        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        if (queryResult != null && !queryResult.isEmpty()) {
            for (Object[] object : queryResult) {
                map.put("id", String.valueOf(object[0]));
                map.put("fname", (String) object[1]);
                map.put("lname", (String) object[2]);
                map.put("email", (String) object[3]);
                map.put("roleId", String.valueOf(object[5]));
                map.put("netAmount", String.valueOf(object[6]));
                result.add(map);
                map = new HashMap<String, String>();
            }
        }
        return result;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/netPerUser/{d1}/{d2}") // returns a list all user details along with their total expenses
    public ResponseEntity<Iterable<HashMap<String, String>>> getNetPerUser(@PathVariable("d1") Date d1, @PathVariable("d2") Date d2) {
        List<Object[]> queryResult = expenseRepository.getNetPerUser(d1, d2);
        return new ResponseEntity<>(mapUserData(queryResult), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/netPerUser") // returns a list all user details along with their total expenses
    public ResponseEntity<Iterable<HashMap<String, String>>> getNetPerUser() {
        List<Object[]> queryResult = expenseRepository.getNetPerUser();
        return new ResponseEntity<>(mapUserData(queryResult), HttpStatus.OK);
    }

    public Iterable<HashMap<String, String>> mapCategoryData(List<Object[]> queryResult){
        HashMap<String, String> map = new HashMap<String, String>();
        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        if(queryResult != null && !queryResult.isEmpty()){
            for (Object[] object : queryResult) {
                map.put("id", String.valueOf(object[0]));
                map.put("CategoryName", (String)object[1]);
                map.put("netAmount", String.valueOf(object[2]));
                result.add(map);
                map = new HashMap<String, String>();
            }
        }
        return result;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/netPerCategory/{d1}/{d2}") // returns a list all user details along with their total expenses
    public ResponseEntity<Iterable<HashMap<String, String>>> getNetPerCategory(@PathVariable("d1") Date d1, @PathVariable("d2") Date d2) {
        List<Object[]> queryResult = expenseRepository.getNetPerCategory(d1, d2);
        return new ResponseEntity<>(mapCategoryData(queryResult), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/netPerCategory") // returns a list all user details along with their total expenses
    public ResponseEntity<Iterable<HashMap<String, String>>> getNetPerCategory() {
        List<Object[]> queryResult = expenseRepository.getNetPerCategory();
        return new ResponseEntity<>(mapCategoryData(queryResult), HttpStatus.OK);
    }
}
