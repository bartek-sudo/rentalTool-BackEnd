package com.example.rentalTool_BackEnd.user.repo;

import com.example.rentalTool_BackEnd.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepo extends JpaRepository<User,Long> {

    Optional<User> findUserByEmail(String email);

    List<User> findUsersByFirstName(String firstName);
}
