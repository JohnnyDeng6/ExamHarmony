package ca.cmpt276.examharmony.Controllers;

import java.time.LocalDateTime;

import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserService;
import ca.cmpt276.examharmony.utils.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequestService;

@Controller
@RequestMapping("/admin")
public class AdminRequestController {

//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private IntervalRepository intervalRepository;
//
//    private final InvigilatorRequestService invigilatorRequestService;
//
//    @Autowired
//    public AdminRequestController(InvigilatorRequestService invigilatorRequestService) {
//        this.invigilatorRequestService = invigilatorRequestService;
//    }
//
//
//    @PostMapping("/sendRequest")
//    public String sendRequest(@RequestHeader(value = "Referer", required = false) String referer,
//            @RequestParam String username,
//            @RequestParam String email,
//            @RequestParam String examCode,
//            @RequestParam String examDate,
//            RedirectAttributes redirectAttributes,
//            Model model) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        User currentUser = userDetails.getCurrentUser();
//        EditInterval interval = intervalRepository.findById(1);
//        User user = userService.findByUsername(username);
//        if (user != null && user.getEmailAddress().equals(email)) {
//            LocalDateTime parsedExamDate = LocalDateTime.parse(examDate);
//            invigilatorRequestService.createRequest(username, email, examCode, parsedExamDate);
//            redirectAttributes.addFlashAttribute("alertMessage", "Request sent successfully!");
//            if(referer == null){
//                model.addAttribute("admin", currentUser);
//                model.addAttribute("interval", interval);
//                return "admin/adminHome";
//            } else {
//                return "redirect:" + referer;
//            }
//        } else {
//            redirectAttributes.addFlashAttribute("alertMessage", "These credentials do not exist");
//            return "redirect:/admin/home";
//        }
//    }
//
//    @GetMapping("/adminTestPage")
//    public String adminTestPage(Model model) {
//        // Logic to fetch and display requests for admins
//        return "admin/adminHome";
//    }
}