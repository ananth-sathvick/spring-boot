package com.example.controller;

import com.example.model.Role;
import com.example.repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/role") // This means URL's start with /demo (after Application path)
public class RoleController {
  @Autowired // This means to get the bean called userRepository // Which is auto-generated
             // by Spring, we will use it to handle the data
  private RoleRepository roleRepository;

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @PostMapping(path = "/add") // Map ONLY POST Requests
  public ResponseEntity<Role> addNewRole(@RequestBody Role role) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request
    return new ResponseEntity<>(roleRepository.save(role), HttpStatus.CREATED);
  }

  @GetMapping(path = "/all")
  public ResponseEntity<Iterable<Role>> getAllRoles() {
    return new ResponseEntity<>(roleRepository.findAll(), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @DeleteMapping(path = "/delete/{id}")
  public ResponseEntity<String> deleteById(@PathVariable int id) {
    try{
        roleRepository.deleteById(id);
    }
    catch(IllegalArgumentException e) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Role Not Found", e); 
    }
    return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
  }

}
