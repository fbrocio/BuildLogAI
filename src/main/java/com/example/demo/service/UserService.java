package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
La capa UserService contiene la lógica de negocio (validaciones, etc)
 */
@Service
public class UserService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService (UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserRequest request){
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Se hashea la contraseña
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }
}
