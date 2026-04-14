package com.train.notificationservice.service;

import com.train.notificationservice.dto.NotificationRequest;
import com.train.notificationservice.entity.Notification;
import com.train.notificationservice.repository.NotificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository,
                               JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    public String sendNotification(NotificationRequest request) {

        Notification notification = new Notification();
        notification.setBookingId(request.getBookingId());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setStatus("SENT");
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        sendEmail(request);

        return "Notification sent successfully";
    }

    private void sendEmail(NotificationRequest request) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(request.getEmail());
        mail.setSubject("Train Booking Update 🚆");
        mail.setText(
                "Booking ID: " + request.getBookingId() + "\n\n" +
                        request.getMessage()
        );

        mailSender.send(mail);
    }
}