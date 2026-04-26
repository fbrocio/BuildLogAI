package com.example.demo.repository;

import com.example.demo.model.UserInput;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInputRepository extends JpaRepository<UserInput, Long> {
}
