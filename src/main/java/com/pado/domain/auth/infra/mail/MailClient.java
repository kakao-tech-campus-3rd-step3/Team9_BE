package com.pado.domain.auth.infra.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailClient{

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String content) {
        SimpleMailMessage msg = createMessage(to, subject, content);
        mailSender.send(msg);
    }

    private static SimpleMailMessage createMessage(String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        return msg;
    }
}
