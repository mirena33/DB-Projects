package com.springdata.restdemo.service;

import com.springdata.restdemo.model.User;

import java.util.Collection;

public interface UserService {
    Collection<User> getUsers();

    User getUserById(Long id);

    User getUserByUsername(String username);

    User addUser(User user);

    User updateUser(User user);

    User deleteUser(Long id);

    long getUsersCount();
}
