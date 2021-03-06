package com.example.service.Impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.example.model.Role;
import com.example.model.User;
import com.example.model.UserDto;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.service.PasswordService;
import com.example.service.RoleService;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    EmailService emailService;

    @Autowired
    PasswordService passwordService;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                getAuthority(user));
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        // user.getRole().forEach(role -> {
        // authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        // });

        Role role = user.getRole();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        return authorities;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userDao.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public User findOne(String username) {
        return userDao.findByEmail(username);
    }

    @Override
    public User save(UserDto user) {

        User nUser = user.getUserFromDto();
        String password = passwordService.GenerateRandomPassword(7);
        user.setPassword(password);
        nUser.setPassword(bcryptEncoder.encode(password));

        Role role = roleService.findRoleByRoleName(user.getRoleName());
        emailService.sendEmail("admin@expense.tracker.com", user.getEmail(), "Welcome to Expense Tracker", "<h1>Welcome to Expense Tracker</h1><h3>Hello, "+ user.getFname() +" "+ user.getLname() +"</h3><p>Please use the below login credentials to login</p>"+
        "<p>"+
        "username :"+ user.getEmail() + "</p><p>" +
        "password :"+ user.getPassword() +
        "</p>" + "<p>You are registered as "+user.getRoleName()+"</p>");
        nUser.setRole(role);

        return userDao.save(nUser);
    }

    
}