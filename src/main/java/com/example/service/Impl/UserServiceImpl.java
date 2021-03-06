package com.example.service.Impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.service.PasswordService;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    PasswordService passwordService;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if(user == null){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                getAuthority(user));
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        Role role = user.getRole();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        return authorities;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public User findOne(String username) {
        return userRepository.findByEmail(username);
    }

    
}