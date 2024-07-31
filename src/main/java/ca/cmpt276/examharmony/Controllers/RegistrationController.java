package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.utils.EmailService;
import ca.cmpt276.examharmony.Model.registration.UserRegistrationDto;
import ca.cmpt276.examharmony.Model.user.UserService;
import ca.cmpt276.examharmony.calendarUtils.CalendarManagementService;
import ca.cmpt276.examharmony.utils.UserAlreadyExistException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

    @Controller
    public class RegistrationController {

        @Autowired
        private UserService userService;

        @Autowired
        private EmailService emailService;

        @Autowired
        private CalendarManagementService calendarManagementService;

        @GetMapping("/admin/register")
        public String showRegistrationForm(Model model) {
            model.addAttribute("user", new UserRegistrationDto());
            return "admin/registrationPage";
        }

        @PostMapping("/admin/registration")
        public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto registrationDto, RedirectAttributes redirectAttributes, HttpSession session, Errors errors) throws NoSuchAlgorithmException {
            UUID prtUUID = userService.registerNewUser(registrationDto);
            try {
                redirectAttributes.addFlashAttribute("alertMessage", "New user registered successfully, a link has been sent to the email. Return home");
                String toEmail = registrationDto.getEmail();
                String subject = "Registration Confirmation";
                String body = buildWelcomeEmailBody(registrationDto, prtUUID);
                emailService.sendHtmlEmail(toEmail, subject, body);

//                //if role is inv then share or admin
//                if (registrationDto.getRoles().contains("INVIGILATOR") || registrationDto.getRoles().contains("ADMIN")) {
//                    String calendarId = "examharmony6@gmail.com";
//                    calendarManagementService.shareCalendarWithUser(calendarId, toEmail);
//                }

            } catch (UserAlreadyExistException uaeEx) {
                redirectAttributes.addFlashAttribute("alertMessage", "Account already exists, please choose another email or username");
                return "redirect:/admin/register";
            } catch (Exception e) {
                String link = "https://examharmony.onrender.com/reset-password?passwordResetToken=" + prtUUID;
                redirectAttributes.addFlashAttribute("alertMessage", "email failed to send, this an link to set the password for the account, do not lose it: " + link);
                return "redirect:/admin/home";
//                throw new RuntimeException(e);

            }
            return "redirect:/admin/home";
        }

        private String buildWelcomeEmailBody(UserRegistrationDto registrationDto, UUID prtUUID) {
            String link = "https://examharmony.onrender.com/reset-password?passwordResetToken=" + prtUUID;
            return "<p>Dear " + registrationDto.getName() + ",</p>"
                    + "<p>Welcome to ExamHarmony! We are thrilled to have you on board.</p>"
                    + "<p>Your unique username is: " + registrationDto.getUsername() + ",</p>"
                    + "<p>To get started, please set your password by clicking the link below:</p>"
                    + "<p><a href=\"" + link + "\">Set Your Password</a></p>"
                    + "<p>If you did not register for an account, please ignore this email.</p>"
                    + "<p>Best regards,</p>"
                    + "<p>The ExamHarmony Team</p>"
                    + "<p><em>Note: This is an automated message, please do not reply.</em></p>";
        }
    }

