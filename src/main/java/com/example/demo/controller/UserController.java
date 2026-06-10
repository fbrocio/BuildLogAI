package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.VerifyRequest;
import com.example.demo.model.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private EmailService emailService;
    public UserController(
            UserService userService,
            EmailService emailService){
        this.userService = userService;
        this.emailService = emailService;
    }

    //REGISTER
    @PostMapping("/register")
    public UserResponse register (@RequestBody UserRequest request){
        return new UserResponse(userService.register(request));
    }

    //LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserRequest request){
        return userService.login(request.getEmail(), request.getPassword());
    }

    @GetMapping("/me")
    public String getUser(HttpServletRequest request){

        // Recupera el userId que previamente ha sido extraído del JWT
        // en el filtro (JwtFilter) y almacenado como atributo de la request.
        // Este valor NO viene del cliente directamente.

        Long userId = (Long) request.getAttribute("userId");
        // Si no hay userId, significa:
        // - No se envió token
        // - El token era inválido
        // - El filtro no lo pudo procesar
        if(userId == null){
            throw new RuntimeException("Usuario no autenticado");
        }

        // En este punto el usuario está autenticado
        // y el ID proviene de un token validado por el backend.
        return "User ID: " + userId;
    }

    @GetMapping("/email")
    public UserResponse getByEmail(
            @RequestParam String email
    ) {

        User user = userService.findByEmail(email);

        return new UserResponse(user);
    }

    @PostMapping("/verify")
    public String verifyEmail(
            @RequestBody VerifyRequest request){

        userService.verifyEmail(
                request.getEmail(),
                request.getCode()
        );

        return "Cuenta verificada correctamente";
    }

    /*@PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestParam String email) {

        userService.resendVerificationCode(email);

        return ResponseEntity.ok("Código reenviado");
    }*/
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestParam String email) {

        System.out.println("CONTROLLER - Email recibido: " + email);

        userService.resendVerificationCode(email);

        return ResponseEntity.ok("Código reenviado");
    }

}
