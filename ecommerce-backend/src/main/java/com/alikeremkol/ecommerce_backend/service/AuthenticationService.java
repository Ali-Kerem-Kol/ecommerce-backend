package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.dto.UserLoginDto;
import com.alikeremkol.ecommerce_backend.dto.UserRegisterDto;
import com.alikeremkol.ecommerce_backend.dto.UserVerifyDto;
import com.alikeremkol.ecommerce_backend.exception.AlreadyExistsException;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Role;
import com.alikeremkol.ecommerce_backend.model.User;
import com.alikeremkol.ecommerce_backend.repository.RoleRepository;
import com.alikeremkol.ecommerce_backend.repository.UserRepository;
import com.alikeremkol.ecommerce_backend.response.LoginResponse;
import jakarta.mail.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final BlacklistTokenService blacklistTokenService;

    public AuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, EmailService emailService, RoleRepository roleRepository, JwtService jwtService, BlacklistTokenService blacklistTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.blacklistTokenService = blacklistTokenService;
    }

    public User signup(UserRegisterDto input) {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new AlreadyExistsException("This username is already in use.");
        }
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new AlreadyExistsException("This email is already in use.");
        }

        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.getRoles().add(userRole);

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        user.setEnabled(false);

        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);

        return savedUser;
    }

    public LoginResponse authenticateUser(UserLoginDto input) {
        User authenticatedUser = authenticate(input);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        return new LoginResponse(jwtToken, jwtService.getExpirationTime());
    }
    public User authenticate(UserLoginDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new SecurityException("Account is not enabled.");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));

        return user;
    }

    public void verifyUser(UserVerifyDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new SecurityException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new SecurityException("Invalid verification code");
            }
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new AlreadyExistsException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public void logout(String authHeader) throws AccessDeniedException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Authorization header missing or invalid");
        }

        String token = authHeader.substring(7);
        Instant expirationTime = jwtService.getExpirationTimeFromTokenAsInstant(token);
        blacklistTokenService.addTokenToBlacklist(token, expirationTime);
    }


}
