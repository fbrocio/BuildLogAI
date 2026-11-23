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
import java.security.SecureRandom;

/*
La capa UserService contiene la lógica de negocio (validaciones, etc)
 */
@Service
public class UserService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final long CODE_VALIDITY_MINUTES = 10;
    private static final long RESEND_COOLDOWN_SECONDS = 60;
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

        String verificationCode = newVerificationCode();

        user.setVerified(false);
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
        user.setVerificationLastSentAt(LocalDateTime.now());
        user.setVerificationAttempts(0);

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

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Do not disclose whether an account exists.
            return;
        }

        if(user.isVerified()){
            throw new RuntimeException(
                    "La cuenta ya está verificada");
        }

        if(user.getVerificationCode() == null || user.getVerificationCodeExpiry() == null
                || LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())){
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);
            throw new RuntimeException(
                    "Código de verificación no válido");
        }

        if (user.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            throw new RuntimeException("Código de verificación no válido");
        }

        if(!user.getVerificationCode().equals(code)){
            user.setVerificationAttempts(user.getVerificationAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException(
                    "Código de verificación no válido");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationAttempts(0);

        userRepository.save(user);
    }

    public void resendVerificationCode(String email) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isVerified()) return;
        if (user.getVerificationLastSentAt() != null &&
                user.getVerificationLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
            return;
        }

        String code = newVerificationCode();

        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
        user.setVerificationLastSentAt(LocalDateTime.now());
        user.setVerificationAttempts(0);

        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Código de verificación BuildLogAI",
                "Tu nuevo código de verificación es: " + code);
    }

    private String newVerificationCode() { return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000)); }
}
