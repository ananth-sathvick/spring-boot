package com.example.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import com.example.config.TokenProvider;
import com.example.model.AuthToken;
import com.example.model.ChangePassword;
import com.example.model.Expense;
import com.example.model.LoginUser;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.ExpenseRepository;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.service.PasswordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(path = "/users")
public class UserController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private TokenProvider jwtTokenUtil;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private BCryptPasswordEncoder bcryptEncoder;

  @Autowired
  private PasswordService passwordService;

  @Autowired
  private EmailService emailService;

  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
  public ResponseEntity<?> generateToken(@RequestBody LoginUser loginUser) throws AuthenticationException {
    //Authenticate an user given email and password, returns JWT Token
    final Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginUser.getEmail(), loginUser.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    final String token = jwtTokenUtil.generateToken(authentication);
    return new ResponseEntity<>(new AuthToken(token), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @RequestMapping(value = "/register/{roleName}", method = RequestMethod.POST)
  public ResponseEntity<User> saveUser(@RequestBody User user, @PathVariable("roleName") String roleName)
      throws SQLIntegrityConstraintViolationException {
        //To add a new user with a unique email Id
    String password = passwordService.GenerateRandomPassword(7);
    Role role = roleRepository.findRoleByRoleName(roleName);
    if (role == null) {
      return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    user.setPassword(bcryptEncoder.encode(password));
    user.setRole(role);

    try {
      emailService.sendEmail("admin@expense.tracker.com", user.getEmail(), "Welcome to Expense Tracker",
          "<h1>Welcome to Expense Tracker</h1><h3>Hello, " + user.getFname() + " " + user.getLname()
              + "</h3><p>Please use the below login credentials to login</p>" + "<p>" + "username :" + user.getEmail()
              + "</p><p>" + "password :" + password + "</p>" + "<p>You are registered as " + roleName + "</p>", null);
      userRepository.save(user);
    } 
    catch (DataIntegrityViolationException e) {
        throw new SQLIntegrityConstraintViolationException("Already Registered!");
      }

    return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);

  }

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @RequestMapping(value = "/adminping", method = RequestMethod.GET)
  public String adminPing() {
    // Tests AdminPing
    return "Only Admins Can Read This";
  }

  @PreAuthorize("hasRole('USER')")
  @RequestMapping(value = "/userping", method = RequestMethod.GET)
  public @ResponseBody User userPing() {
    //Tests user ping
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = userDetails.getUsername();
    return userRepository.findByEmail(username);
  }

  @RequestMapping(value = "/changepassword", method = RequestMethod.POST)
  public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword)
      throws SQLIntegrityConstraintViolationException {
        //User password change route
    if (passwordService.changePassword(changePassword)) {
      return new ResponseEntity<>("Password Changed Successfully", HttpStatus.OK);
    } else {
      throw new SQLIntegrityConstraintViolationException("Current Password Incorrect!");
    }
  }

  @RequestMapping(value = "/forgotpassword", method = RequestMethod.POST)
  public ResponseEntity<String> forgotPassword(@RequestBody Map<String, Object> jsonEmail){
    //To get new password created to registered email Id
    String email = (String) jsonEmail.get("email");
    if (passwordService.forgotPassword(email)) {
      return new ResponseEntity<>("New Password sent to your Email-Id successfully", HttpStatus.OK);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user found!");
    }
  }

@PreAuthorize("hasRole('ADMIN')") // Admin only
@RequestMapping(value="/getallusers",method=RequestMethod.GET)
  public Iterable<User> getAllUsers() {
    //List all the users with role USER
    return userRepository.getAll(1);
  }

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @RequestMapping(value = "/getalladmins", method = RequestMethod.GET)
  public Iterable<User> getAllAdmins() {
    //List all the users with role ADMIN
    return userRepository.getAll(2);
  }

  @PreAuthorize("hasRole('ADMIN')") // Admin only
  @RequestMapping(value = "/delete/{uid}", method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteUser(@PathVariable("uid") Integer uid) {
    //Delete a user with id = {id}
    Optional<User> ouser = userRepository.findById(uid);
    if(ouser.isPresent()){
      ArrayList<Expense> expenses = (ArrayList<Expense>)expenseRepository.getByUser(uid);
      for(int i = 0; i < expenses.size(); i++)
        expenseRepository.deleteById(expenses.get(i).getId());
      userRepository.deleteById(uid);
      return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }
    return new ResponseEntity<>("User not found", HttpStatus.CONFLICT);
  }

}
