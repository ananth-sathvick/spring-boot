package com.example.controller;

import com.example.model.Category;
import com.example.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Controller
@RequestMapping(path = "/category")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping(path = "/all") // returns list of all categories
    public ResponseEntity<Iterable<Category>> getAllCategorys() {
        return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/find/{id}") //returns category with id = {id}
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        if(category.isPresent())
            return new ResponseEntity<>(category.get(), HttpStatus.OK);
        else
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such category found!");
	}
	
    @PreAuthorize("hasRole('ADMIN')") // Admin only
	@PostMapping("/add") // adds new category to category tables and returns it
	public ResponseEntity<Category> addCategory(@RequestBody Category category){
		Category newCategory = categoryRepository.save(category);
		return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
	}
	
    @PreAuthorize("hasRole('ADMIN')") // Admin only
	@PutMapping("/update") // updates an existing category and returns it with updated values
	public ResponseEntity<Category> updateCategory(@RequestBody Category category){
    	try {
			Category updateCategory = categoryRepository.save(category);
			return new ResponseEntity<>(updateCategory, HttpStatus.OK);
    	}
    	catch(IllegalArgumentException e) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such category found!");
    	}
	}
	
    @PreAuthorize("hasRole('ADMIN')") // Admin only
	@DeleteMapping("/delete/{id}") // deletes the category with id = {id}
	public ResponseEntity<?> deleteCategory(@PathVariable("id") Integer id){
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else
        {
    		throw new ResponseStatusException(HttpStatus.NOT_MODIFIED, "No such category found!");
        }
	}
}
