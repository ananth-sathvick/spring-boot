package com.example.controller;

import com.example.config.TokenProvider;
import com.example.model.AuthToken;
import com.example.model.LoginUser;
import com.example.model.User;
import com.example.model.UserDto;
import com.example.repository.UserRepository;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private TokenProvider jwtTokenUtil;

  @Autowired
  private UserService userService;

  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
  public ResponseEntity<?> generateToken(@RequestBody LoginUser loginUser) throws AuthenticationException {

      final Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      loginUser.getUsername(),
                      loginUser.getPassword()
              )
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);
      final String token = jwtTokenUtil.generateToken(authentication);
      return ResponseEntity.ok(new AuthToken(token));
  }

  @RequestMapping(value="/register", method = RequestMethod.POST)
  public User saveUser(@RequestBody UserDto user){
      return userService.save(user);
  }



  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value="/adminping", method = RequestMethod.GET)
  public String adminPing(){
      return "Only Admins Can Read This";
  }
  
  @Autowired
  UserRepository userRepository;
  @PreAuthorize("hasRole('USER')")
  @RequestMapping(value="/userping", method = RequestMethod.GET)
  public @ResponseBody User userPing(){
    UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = userDetails.getUsername();
    
    User user = userRepository.findByEmail(username);
    return user;
  }

  

}
