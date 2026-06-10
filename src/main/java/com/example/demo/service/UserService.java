package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.UserRequest;
import com.example.demo.exception.EmailNotVerifiedException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;

/*
La capa UserService contiene la lógica de negocio (validaciones, etc)
 */
@Service
public class UserService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public UserService (UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        EmailService emailService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    // REGISTRO
    public User register(UserRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email ya registrado");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        user.setPassword(hashedPassword);

        String verificationCode =
                String.format("%06d",
                        new Random().nextInt(1000000));

        user.setVerified(false);
        user.setVerificationCode(verificationCode);

        User savedUser = userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Código de verificación BuildLogAI",
                "Tu código de verificación es: "
                        + verificationCode
        );

        return savedUser;
    }
    // LOGIN
    public AuthResponse login(String email, String password){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Contraseña incorrecta");
        }
        if(!user.isVerified()){
            throw new EmailNotVerifiedException();
        }

        String token = jwtService.generateToken(user.getId());

        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public User findByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }

    public void verifyEmail(String email, String code){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if(user.isVerified()){
            throw new RuntimeException(
                    "La cuenta ya está verificada");
        }

        if(user.getVerificationCode() == null){
            throw new RuntimeException(
                    "No existe código de verificación");
        }

        if(!user.getVerificationCode().equals(code)){
            throw new RuntimeException(
                    "Código incorrecto");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);

        userRepository.save(user);
    }

    public void resendVerificationCode(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        String code = String.format(
                "%06d",
                new Random().nextInt(1000000)
        );

        user.setVerificationCode(code);

        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Código de verificación BuildLogAI",
                "Tu nuevo código de verificación es: "
                        + code
        );
    }
}
