package com.taskremainder.app.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private boolean isPlaceholderEmail() {
        return senderEmail == null || senderEmail.contains("placeholder") || senderEmail.contains("example");
    }

    public void sendWelcomeEmail(String to) {
        if (isPlaceholderEmail()) {
            System.out.println("⚠️ EMAIL SIMULATION (Welcome): Email sending skipped because placeholder credentials are in use. To=" + to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject("Welcome to Task Reminder");
            message.setText("Your account has been created successfully.");
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("⚠️ EMAIL SIMULATION (Welcome): To=" + to);
            // Suppress error for demo purposes so registration succeeds even if mail config is missing
        }
    }

    public void sendTaskReminder(String to, String taskTitle, String dueDate) {
        if (isPlaceholderEmail()) {
            System.out.println("⚠️ EMAIL SIMULATION (Reminder): To=" + to + ", Task=" + taskTitle);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject("Task Reminder: " + taskTitle);
            message.setText("Hi,\n\nThis is a reminder that your task '" + taskTitle + "' is due on " + dueDate + ".\n\nPlease complete it soon!\n\nBest,\nTask Reminder App");
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("⚠️ EMAIL SIMULATION (Reminder): To=" + to + ", Task=" + taskTitle);
        }
    }


    public void sendOtpEmail(String to, String otp) {
        // ALWAYS Log OTP to console for local testing/demo
        System.out.println("\n==================================================");
        System.out.println("🔐 OTP for " + to + ": " + otp);
        System.out.println("==================================================\n");

        if (isPlaceholderEmail()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + "\nIt expires in 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ FAILED TO SEND EMAIL to " + to);
            System.err.println("Reason: " + e.getMessage());
            
            // Only print stack trace if it's NOT an authentication error (to reduce noise for demo)
            boolean isAuthError = e instanceof org.springframework.mail.MailAuthenticationException 
                               || e.getCause() instanceof jakarta.mail.AuthenticationFailedException;
            
            if (!isAuthError) {
                e.printStackTrace();
            } else {
                System.err.println("👉 (Stack trace suppressed for Authentication Error)");
            }
            
            System.err.println("👉 Please check 'application.properties' and ensure:");
            System.err.println("   1. spring.mail.username (" + senderEmail + ") is your real Gmail address");
            System.err.println("   2. spring.mail.password is your 16-character App Password (NOT your login password)");
            System.err.println("   3. Two-Factor Authentication (2FA) is ENABLED on your Google Account to generate an App Password.");
        }
    }

    public void sendCsvReport(String to,
                              String subject,
                              String body,
                              byte[] attachmentData,
                              String attachmentName) {

        if (isPlaceholderEmail()) {
            System.out.println("⚠️ EMAIL SIMULATION (Report): To=" + to);
            return;
        }

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            if (attachmentData == null || attachmentData.length == 0) {
                throw new IllegalArgumentException("CSV attachment is EMPTY");
            }

            helper.addAttachment(
                    attachmentName,
                    new org.springframework.core.io.ByteArrayResource(attachmentData)
            );

            mailSender.send(message);
            System.out.println("✅ CSV Report Mail Sent to " + to);

        } catch (Exception e) {
            System.err.println("❌ FAILED TO SEND CSV REPORT MAIL");
            e.printStackTrace();
        }
    }

}
