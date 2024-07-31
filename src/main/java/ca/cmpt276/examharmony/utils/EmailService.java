package ca.cmpt276.examharmony.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        emailSender.send(message);
    }


    public void sendEmailWithBCC(String[] to, String subject, String body) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

//        message.setFrom("noreply.examharmony@gmail.com");
        helper.setTo("noreply.examharmony@gmail.com");
        helper.setBcc(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        emailSender.send(message);
    }

    public String buildEditingPeriodEmailBody(LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        return "<p>Dear all users,</p>"
                + "<p>This is an automated message sent to all Invigilators and Instructors.</p>"
                + "<p>The editing period has been set/updated by administration.</p>"
                + "<p>You may start editing within the set period from <strong>" + formattedStartDate + "</strong> to <strong>" + formattedEndDate + "</strong>:</p>"
                + "<p>If you are an Invigilator, you will be able to set your availability and accept/reject requests within the set period. "
                + "If you are an Instructor, you will be able to request your desired exam slot preference within the set period.</p>"
                + "<p>If you are not a user of ExamHarmony, please ignore this email.</p>"
                + "<p>Best regards,</p>"
                + "<p>The ExamHarmony Team</p>"
                + "<p><em>Note: This is an automated message, please do not reply.</em></p>";
    }

    public String buildPasswordResetEmailBody(String name, UUID prtUUID) {
        String link = "https://examharmony.onrender.com/reset-password?passwordResetToken=" + prtUUID;
        return "<p>Dear " + name + ",</p>"
                + "<p>You have requested a password reset.</p>"
                + "<p>To get started, please set your password by clicking the link below:</p>"
                + "<p><a href=\"" + link + "\">Set Your Password</a></p>"
                + "<p>This link will expire in 24 hours</p>"
                + "<p>If you did not request for a password reset, please ignore this email.</p>"
                + "<p>Best regards,</p>"
                + "<p>The ExamHarmony Team</p>"
                + "<p><em>Note: This is an automated message, please do not reply.</em></p>";
    }
}
