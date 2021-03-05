package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/demo") // This means URL's start with /demo (after Application path)
public class UserController {
  @Autowired // This means to get the bean called userRepository // Which is auto-generated
             // by Spring, we will use it to handle the data
  private UserRepository userRepository;

  @PostMapping(path = "/add") // Map ONLY POST Requests
  public @ResponseBody String addNewUser(@RequestBody User user) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request

    userRepository.save(user);
    return "Saved";
  }

  @GetMapping(path = "/all")
  public @ResponseBody Iterable<User> getAllUsers() {
    // This returns a JSON or XML with the users
    return userRepository.findAll();
  }

  // This is how to use email service 
  @Autowired
  EmailService emailService;

  @GetMapping(path = "/testmail")
  public @ResponseBody String sendMail() {

    emailService.sendEmail("admin.expense-tracker@accolitedigital.com","ananthsathvick@gmail.com","Test email","<h1>Test email</h1>");
    return "Sent";
  }
  //Email service end
}
