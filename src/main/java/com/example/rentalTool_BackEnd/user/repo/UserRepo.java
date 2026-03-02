package com.example.rentalTool_BackEnd.user.repo;

import com.example.rentalTool_BackEnd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepo {

    private final UserJpaRepo userJpaRepo;

    public Optional<User> findUserByEmail(String email) {
        return userJpaRepo.findUserByEmail(email);
    }

    public Optional<User> findUserById(long id){
        return userJpaRepo.findById(id);
    }

    public User createUser(User user){
        return userJpaRepo.save(user);
    }

    public User updateUser(User user){
        return userJpaRepo.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userJpaRepo.findAll(pageable);
    }

    public Page<User> findUsersBySearch(String search, Pageable pageable) {
        return userJpaRepo.findUsersBySearch(search, pageable);
    }
}
