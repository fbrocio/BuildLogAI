package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.UserRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/*
La capa UserService contiene la lógica de negocio (validaciones, etc)
 */
@Service
public class UserService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService (UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTRO
    public User register(UserRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Email ya registrado");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Se hashea la contraseña
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    // LOGIN
    public AuthResponse login(String email, String password){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtService.generateToken(user.getId());

        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public User findByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }
}
