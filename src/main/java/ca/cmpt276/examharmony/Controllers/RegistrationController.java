package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.emailSender.EmailService;
import ca.cmpt276.examharmony.Model.registration.UserRegistrationDto;
import ca.cmpt276.examharmony.Model.user.UserService;
import ca.cmpt276.examharmony.calendarUtils.CalendarManagementService;
import ca.cmpt276.examharmony.utils.UserAlreadyExistException;
import jakarta.mail.MessagingException;
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

// When an admin creates an account, assume they know the person’s email
    // and create a temporary password, role(s), and username for them,
    // then they are sent the email with their account,
    // from then they can log in and change their password or username

    //create tmp pw,
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
            return "registrationPage";
        }

        @PostMapping("/admin/registration")
        public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto registrationDto, RedirectAttributes redirectAttributes, HttpSession session, Errors errors) {
            try {
                UUID prtUUID = userService.registerNewUser(registrationDto);
                redirectAttributes.addFlashAttribute("alertMessage", "New user registered successfully, a link has been sent to the email. Return home");
                String toEmail = registrationDto.getEmail();
                String subject = "Registration Confirmation";
                String body = buildWelcomeEmailBody(registrationDto, prtUUID);
                emailService.sendHtmlEmail(toEmail, subject, body);

                //if role is inv then share or admin
                if (registrationDto.getRoles().contains("INVIGILATOR") || registrationDto.getRoles().contains("ADMIN")) {
                    String calendarId = "examharmony6@gmail.com";
//                    String calendarId = "42a120091e519ed6d2e9d6372d5cfb188ee4d14d6362c7c8527192fc10b67994@group.calendar.google.com";
                    calendarManagementService.shareCalendarWithUser(calendarId, toEmail);
                }

            } catch (UserAlreadyExistException uaeEx) {
                redirectAttributes.addFlashAttribute("alertMessage", "Account already exists, please choose another email or username");
                return "redirect:/admin/register";
            } catch (Exception e) {
                throw new RuntimeException(e);
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

