package com.example.controller;

import java.util.Map;

import com.example.config.TokenProvider;
import com.example.model.AuthToken;
import com.example.model.ChangePassword;
import com.example.model.LoginUser;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.service.PasswordService;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
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
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private BCryptPasswordEncoder bcryptEncoder;

  @Autowired
  private PasswordService passwordService;


  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
  public ResponseEntity<?> generateToken(@RequestBody LoginUser loginUser) throws AuthenticationException {


      final Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginUser.getEmail(), loginUser.getPassword())
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);
      final String token = jwtTokenUtil.generateToken(authentication);
      return new ResponseEntity<>(new AuthToken(token), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value="/register/{roleName}", method = RequestMethod.POST)
  public ResponseEntity<User> saveUser(@RequestBody User user, @PathVariable("roleName") String roleName){
      user.setPassword(bcryptEncoder.encode(user.getPassword()));
      Role role = roleRepository.findRoleByRoleName(roleName);        
      user.setRole(role);
      return new ResponseEntity<> (userRepository.save(user), HttpStatus.CREATED);
  }


  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value = "/adminping", method = RequestMethod.GET)
  public String adminPing() {
    return "Only Admins Can Read This";
  }

  

  @PreAuthorize("hasRole('USER')")
  @RequestMapping(value = "/userping", method = RequestMethod.GET)
  public @ResponseBody User userPing() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = userDetails.getUsername();
    return userRepository.findByEmail(username);
  }


  

  @PreAuthorize("hasRole('USER')")
  @RequestMapping(value="/changepassword", method = RequestMethod.POST)
  public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword){
      if(passwordService.changePassword(changePassword)){
        return new ResponseEntity<>("Password Changed Successfully", HttpStatus.OK);
      }
      return new ResponseEntity<>("Current password is incorrect", HttpStatus.BAD_REQUEST);
  }

  @RequestMapping(value="/forgotpassword", method = RequestMethod.POST)
  public ResponseEntity<String> forgotPassword(@RequestBody Map<String,Object> jsonEmail){
      String email = (String) jsonEmail.get("email");
      if(passwordService.forgotPassword(email))
      {
        return new ResponseEntity<>("New Password sent to your Email-Id successfully", HttpStatus.OK);
      }
      return new ResponseEntity<>("No such user found!", HttpStatus.BAD_REQUEST);
  }

}
