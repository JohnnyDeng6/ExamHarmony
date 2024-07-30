package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;
import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.EditInterval.EditIntervalDTO;
import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequestRepository;
import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequestService;
import ca.cmpt276.examharmony.Model.emailSender.EmailService;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequest;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestRepository;
import ca.cmpt276.examharmony.Model.examSlot.examSlot;
import ca.cmpt276.examharmony.Model.examSlot.examSlotRepository;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestService;

import ca.cmpt276.examharmony.calendarUtils.CalendarManagementService;
import ca.cmpt276.examharmony.utils.CustomUserDetails;
import ca.cmpt276.examharmony.utils.DatabaseService;
import ca.cmpt276.examharmony.utils.InstructorExamSlotRepository;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ca.cmpt276.examharmony.Model.roles.RoleRepository;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestService;

@Controller
@RequestMapping("/admin")
public class Admin {

    @Autowired
    private examSlotRepository examRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ExamSlotRequestRepository examRequestRepository;

    @Autowired
    private IntervalRepository intervalRepository;

    @Autowired
    private  InstructorExamSlotRepository instructorExamRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InvigilatorRequestService invService;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ExamSlotRequestService insService;

    @Autowired
    private CalendarManagementService calendarManagementService;

    @GetMapping("/createEvent")
    public String creatEvent(Model model) {
        return "form";
    }

//   @GetMapping("/shareCal")
//   public String shareCal(Model model) throws Exception {
//       String calendarId = "examharmony6@gmail.com";
////       String calendarId = "42a120091e519ed6d2e9d6372d5cfb188ee4d14d6362c7c8527192fc10b67994@group.calendar.google.com";
//       List<String> allEmails = userRepository.findAllEmailAddresses();
//       calendarManagementService.shareCalendarWithUsers(calendarId, allEmails);
//       System.out.println("HERE");
//       return "redirect:/admin/home";
//   }
//
//    @PostMapping("/createEvent")
//    public String createEvent(
//            @RequestParam String summary,
//            @RequestParam String description,
//            @RequestParam String location,
//            @RequestParam String startDateTime,
//            @RequestParam String endDateTime,
//            Model model) {
//        try {
////            String calendarId = "42a120091e519ed6d2e9d6372d5cfb188ee4d14d6362c7c8527192fc10b67994@group.calendar.google.com";
//            String calendarId = "examharmony6@gmail.com";
//            calendarManagementService.createEvent(calendarId, summary, description, location, startDateTime, endDateTime);
//            model.addAttribute("message", "Event created successfully");
//            return "eventSuccess";
//        } catch (Exception e) {
//            e.printStackTrace();
//            model.addAttribute("alertMessage", "Error occurred while creating the event.");
//            return "redirect:/admin/home";
//        }
//    }

    @GetMapping("/viewRequests")
    public String viewRequests(Model model) {
        List<ExamSlotRequest> examSlotRequests = examRequestRepository.findAll();
        model.addAttribute("examSlotRequests", examSlotRequests);
        return "viewRequests";
    }

    @PostMapping("/approveRequest")
    public String approveRequest(@RequestParam("requestId") int requestId) {
        ExamSlotRequest request = examRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("APPROVED");
            request.setPreferenceStatus(1);
            User owner = userRepository.findByUsername(request.getInstructorName());
            //-------------------------------------
            examSlot exam = new examSlot();
            exam.setStartTime(request.getExamDate());
            exam.setDuration(request.getExamDuration());
            exam.setNumOfRooms(-1);
            exam.setNumInvigilator(-1);
            exam.setAssignedRooms("EMPTY");
            exam.setStatus(request.getStatus());
        
            CoursesSec CourseID = courseRepo.findByCourseName(request.getCourseName());
            exam.setCourseID(CourseID);
        
            examRepo.save(exam);

            //------------------------------------
            Iterator<ExamSlotRequest> iterator = owner.getExamSlotRequests().iterator();
            examRequestRepository.save(request);
            while (iterator.hasNext()){
                ExamSlotRequest currentRequest = iterator.next();
                if(currentRequest.getCourseName().equals(request.getCourseName()) && currentRequest.getID() != request.getID()){
                    instructorExamRepo.removeUserExamRequestAssociation(owner.getUUID(), currentRequest.getID());
                    examRequestRepository.delete(currentRequest);
                    iterator.remove();
                }
            }
        }
        return "redirect:/admin/viewRequests";
    }

    @GetMapping("/viewInstructors")
    public String viewInstructors(Model model) {
        List<User> instructors = userRepository.findByRoleName("INSTRUCTOR");
    
        // Fetch and set exam slot requests for each instructor
        for (User instructor : instructors) {
            List<ExamSlotRequest> requests = insService.getRequests(instructor.getUsername());
            instructor.setExamSlotRequests(requests); // Using your provided setter method
        }
    
        model.addAttribute("instructors", instructors);
        return "viewInstructors";
    }
    


    @GetMapping("/viewInvigilators")
    public String viewInvigilators(Model model) {
        List<User> invigilators = userRepository.findByRoleName("INVIGILATOR");
        model.addAttribute("invigilators",invigilators);
        model.addAttribute("invigilatorService", invService);
        return "viewInvigilators";
    }

    @PostMapping("/add/interval")
    public String setInterval(@RequestBody EditIntervalDTO intervalDTO, Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            EditInterval interval = intervalRepository.findById(1);
            try{
                interval.setTimes(intervalDTO.startDate, intervalDTO.endDate);
                User admin = userRepository.findByUsername(userDetails.getUsername());
                model.addAttribute("admin", admin);
                model.addAttribute("interval", interval);
                intervalRepository.save(interval);
                return "adminHome";

            } catch (RuntimeException err){
                throw new InstructorController.BadRequest(err.getMessage());
            }
        }
        return "redirect:/admin/home";
    }

    @PostMapping("/emailAll")
    public String emailAll(Model model, RedirectAttributes redirectAttributes) throws MessagingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            List<String> allEmails = userRepository.findAllEmailAddresses();
            String[] to = allEmails.toArray(new String[0]);

            String subject = "Editing period date set";
            EditInterval interval = intervalRepository.findById(1);
            String body = emailService.buildEditingPeriodEmailBody(interval.getStartTime(), interval.getEndTime());

            emailService.sendEmailWithBCC(to, subject, body);
        }
        redirectAttributes.addFlashAttribute("alertMessage", "Failed to send mass email, please try again in 24 hours");
        return "redirect:/admin/home";

    }

    @PostMapping("/clearDB")
    public ResponseEntity<String> clearDatabase() {
        databaseService.clearDatabase();
        return ResponseEntity.ok("Database successfully reset");
    }


}



