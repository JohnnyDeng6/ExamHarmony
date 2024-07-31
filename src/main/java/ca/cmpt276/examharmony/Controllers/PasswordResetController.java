package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.utils.CustomUserDetails;
import ca.cmpt276.examharmony.utils.EmailService;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import ca.cmpt276.examharmony.Model.user.UserService;
import ca.cmpt276.examharmony.utils.HashUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/reset-password")
    public String showSetPasswordForm(@RequestParam("passwordResetToken") UUID passwordResetToken, Model model) throws NoSuchAlgorithmException {
        //verify the prtUUID
        HashUtils hashUtils = new HashUtils();
        User user = userService.findByPasswordResetToken(hashUtils.SHA256(passwordResetToken));

        if (user == null || !user.isPasswordResetTokenValid()) {
            model.addAttribute("error", "This link does not exist");
            return "general/reset-password-error";
        }
        model.addAttribute("passwordResetToken", passwordResetToken);
        return "general/reset-password-form";
    }

    @PostMapping("/{prefix}/sendPrt")
    @ResponseBody
    public String sendPasswordResetLink(@PathVariable("prefix") String prefix, RedirectAttributes redirectAttributes) throws NoSuchAlgorithmException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getCurrentUser();

        UUID prtUUID = getNewPrt(user);
        try {
            String toEmail = userDetails.getEmail();
            String subject = "Password Reset Confirmation";
            String body = emailService.buildPasswordResetEmailBody(userDetails.getName(), prtUUID);
            emailService.sendHtmlEmail(toEmail, subject, body);

            return "success";
        } catch (Exception e) {
            String link = "https://examharmony.onrender.com/reset-password?passwordResetToken=" + prtUUID;
            e.printStackTrace();
            return "email failed to send, this is a link to reset your password: " + link;
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "general/forgot-password-form";
    }
    @PostMapping("/forgot-password")
    public String getUserForm(@RequestParam("email") String email, @RequestParam("username") String username, Model model) {
        User user = userService.findByUsername(username);
        if (user != null && user.getEmailAddress().equals(email)) {
            try {
                UUID prtUUID = getNewPrt(user);
                String subject = "Password Reset Confirmation";
                String body = emailService.buildPasswordResetEmailBody(user.getName(), prtUUID);
                emailService.sendHtmlEmail(email, subject, body);
                return "redirect:/login";

            } catch (MessagingException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else {
            model.addAttribute("error", "User not found.");
            return "general/forgot-password-form";
        }
    }

    private UUID getNewPrt(User user) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        UUID prtUUID = new UUID(secureRandom.nextLong(), secureRandom.nextLong());
        HashUtils hashUtils = new HashUtils();
        user.setPasswordResetToken(hashUtils.SHA256(prtUUID));

        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        return prtUUID;
    }


    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("passwordResetToken") UUID passwordResetToken,
                                      @RequestParam("password") String newPassword,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model) throws NoSuchAlgorithmException {
        HashUtils hashUtils = new HashUtils();
        User user = userService.findByPasswordResetToken(hashUtils.SHA256(passwordResetToken));
        if (user == null || !user.isPasswordResetTokenValid()) {
            model.addAttribute("error", "This link does not exist.");
            return "general/reset-password-error";
        }
        model.addAttribute("passwordResetToken", passwordResetToken);
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "general/reset-password-form";
        }
        if (!newPassword.matches("^(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z\\d@$!%*?&]{8,}$")) {
            model.addAttribute("error", "Password must be at least 8 characters long and include a number and a symbol.");
            return "general/reset-password-form";
        }
        userService.updatePassword(user.getUUID(), newPassword);
        userService.invalidatePasswordResetToken(user.getUUID());
        return "redirect:/login";
    }

}

