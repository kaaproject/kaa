package org.kaaproject.tutorials;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

public class EmailService {

  private final String USERNAME = "apasika@kaaiot.io"; // Email from the GMAIL account from which emails will be sent
  private final String PASSWORD = "Blablabla1"; // Password from the email box. Keep it in secret.

  private final JavaMailSenderImpl mailSender;

  public EmailService() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("smtp.gmail.com");
    mailSender.setPort(587);
    mailSender.setUsername(USERNAME);
    mailSender.setPassword(PASSWORD);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");
    this.mailSender = mailSender;
  }

  public void sendNotification(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    this.mailSender.send(message);
  }
}
