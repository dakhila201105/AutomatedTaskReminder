package com.taskremainder.app.controller;

import com.taskremainder.app.entity.User;
import com.taskremainder.app.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Show Login Page
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // Show Welcome Page
    @GetMapping("/")
    public String showWelcomePage() {
        return "welcome";
    }

    // Show Register Page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle Register Form Submission
    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {

        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Account created successfully! Please verify your email."
            );
            return "redirect:/verify-otp?email=" + user.getEmail();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
            return "redirect:/register";
        }
    }

    // Show Verify OTP Page
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@org.springframework.web.bind.annotation.RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    // Handle OTP Verification
    @PostMapping("/verify-otp")
    public String verifyOtp(
            @org.springframework.web.bind.annotation.RequestParam("email") String email,
            @org.springframework.web.bind.annotation.RequestParam("otp") String otp,
            RedirectAttributes redirectAttributes) {

        if (userService.verifyOtp(email, otp)) {
            redirectAttributes.addFlashAttribute("success", "Email verified successfully! Please login.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP.");
            return "redirect:/verify-otp?email=" + email;
        }
    }
    
    // Resend OTP
    @PostMapping("/resend-otp")
    public String resendOtp(
            @org.springframework.web.bind.annotation.RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        
        try {
            userService.resendOtp(email);
            redirectAttributes.addFlashAttribute("success", "OTP resent successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error resending OTP.");
        }
        return "redirect:/verify-otp?email=" + email;
    }
}
