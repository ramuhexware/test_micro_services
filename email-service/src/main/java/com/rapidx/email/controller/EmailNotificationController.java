package com.rapidx.email.controller;

import com.rapidx.email.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class EmailNotificationController {

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        log.info("Email Notification Service: Received request to send email.");
        log.info("To: {}", request.getRecipient());
        log.info("Subject: {}", request.getSubject());
        log.info("Body: {}", request.getBody());
        
        // Simulating email dispatch logic
        log.info("Email successfully dispatched to {}", request.getRecipient());
        
        return ResponseEntity.ok("Email successfully sent.");
    }
}
