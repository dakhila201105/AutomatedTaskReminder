package com.taskremainder.app.service;

import com.taskremainder.app.entity.User;
import com.taskremainder.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final SecureRandom secureRandom = new SecureRandom();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User register(User user) {
        java.util.Optional<User> existingUserOpt = userRepository.findByEmail(user.getEmail());
        
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isEnabled()) {
                throw new RuntimeException("Email already registered");
            } else {
                // User exists but not verified -> Resend OTP and update password if changed
                existingUser.setPassword(passwordEncoder.encode(user.getPassword())); // Update password in case they forgot
                
                String otp = String.format("%06d", secureRandom.nextInt(1000000));
                existingUser.setOtp(otp);
                existingUser.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
                
                userRepository.save(existingUser);
                emailService.sendOtpEmail(existingUser.getEmail(), otp);
                return existingUser;
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // User is not enabled until OTP verification
        user.setRole("USER");
        
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));

        User savedUser = userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return savedUser;
    }

    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            return false;
        }

        if (user.getOtp().equals(otp) && user.getOtpExpiry().isAfter(java.time.LocalDateTime.now())) {
            user.setEnabled(true);
            user.setOtp(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            emailService.sendWelcomeEmail(user.getEmail()); // Send welcome email after verification
            return true;
        }
        return false;
    }
    
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public User getCurrentUser() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("No authentication data found");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public void updateProfile(String name, org.springframework.web.multipart.MultipartFile file) {
        User user = getCurrentUser();
        user.setName(name);

        if (file != null && !file.isEmpty()) {
            try {
                // Save file logic here. For simplicity, we'll store it in a 'uploads' directory or just encode base64 if small.
                // Let's use Base64 for simplicity to avoid dealing with file serving configuration for now, or just save to disk.
                // Saving to disk is better.
                // C:\Users\akhil\Downloads\demo\demo\src\main\resources\static\images\profiles
                
                String uploadDir = "src/main/resources/static/images/profiles/";
                java.io.File directory = new java.io.File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
                java.nio.file.Files.write(filePath, file.getBytes());
                
                user.setProfilePhoto("/images/profiles/" + fileName);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }
        userRepository.save(user);
    }
}
