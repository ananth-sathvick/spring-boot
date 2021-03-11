package com.example.controller;

import com.example.model.Role;
import com.example.repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController // This means that this class is a Controller
@CrossOrigin
@RequestMapping(path = "/role") 
public class RoleController {
  private RoleRepository roleRepository;

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @PostMapping(path = "/add") 
  public ResponseEntity<Role> addNewRole(@RequestBody Role role) {
    // Admin can add new Role
    return new ResponseEntity<>(roleRepository.save(role), HttpStatus.CREATED);
  }

  @GetMapping(path = "/all")
  public ResponseEntity<Iterable<Role>> getAllRoles() {
    // To view all the roles
    return new ResponseEntity<>(roleRepository.findAll(), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @DeleteMapping(path = "/delete/{id}")
  public ResponseEntity<String> deleteById(@PathVariable int id) {
    //To delete a role with particular Id = {id}
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
