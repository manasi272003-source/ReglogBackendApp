package com.reglog.auth;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class OtpMailer {
 private final ObjectProvider<JavaMailSender> sender;private final String from;
 public OtpMailer(ObjectProvider<JavaMailSender> sender,@Value("${reglog.otp.sender}") String from){this.sender=sender;this.from=from;}
 public void send(String to,String otp){JavaMailSender mail=sender.getIfAvailable();if(mail==null)throw new IllegalStateException("SMTP is not configured; set SMTP_HOST and SMTP credentials");SimpleMailMessage m=new SimpleMailMessage();m.setFrom(from);m.setTo(to);m.setSubject("Your Reglog verification code");m.setText("Your verification code is "+otp+". It expires in 10 minutes. If you did not request this, ignore this email.");mail.send(m);}
}
