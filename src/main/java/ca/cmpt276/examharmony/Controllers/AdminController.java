package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;
import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.EditInterval.EditIntervalDTO;
import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequestService;
import ca.cmpt276.examharmony.Model.emailSender.EmailService;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequest;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestRepository;
import ca.cmpt276.examharmony.Model.examSlot.examSlot;
import ca.cmpt276.examharmony.Model.examSlot.examSlotRepository;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestService;
import ca.cmpt276.examharmony.Model.user.UserService;

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

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.cmpt276.examharmony.Model.roles.RoleRepository;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private examSlotRepository examRepo;

    @Autowired
    private UserRepository userRepository;

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
    private UserService userService;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ExamSlotRequestService insService;

//    private final InvigilatorRequestService invigilatorRequestService;
//
//    @Autowired
//    public AdminRequestController(InvigilatorRequestService invigilatorRequestService) {
//        this.invigilatorRequestService = invigilatorRequestService;
//    }

    @GetMapping("/home")
    public String adminTest(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            User admin = userRepository.findByUsername(userDetails.getUsername());
            EditInterval interval = intervalRepository.findById(1);
            model.addAttribute("interval", interval);
            model.addAttribute("admin", admin);
            return "admin/adminHome";
        }
        return "redirect:/login";

    }

    @PostMapping("/sendRequest")
    public String sendRequest(@RequestHeader(value = "Referer", required = false) String referer,
                              @RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String examCode,
                              @RequestParam String examDate,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getCurrentUser();
        EditInterval interval = intervalRepository.findById(1);
        User user = userService.findByUsername(username);
        if (user != null && user.getEmailAddress().equals(email)) {
            LocalDateTime parsedExamDate = LocalDateTime.parse(examDate);
            invService.createRequest(username, email, examCode, parsedExamDate);
            redirectAttributes.addFlashAttribute("alertMessage", "Request sent successfully!");
            if(referer == null){
                model.addAttribute("admin", currentUser);
                model.addAttribute("interval", interval);
                return "admin/adminHome";
            } else {
                return "redirect:" + referer;
            }
        } else {
            redirectAttributes.addFlashAttribute("alertMessage", "These credentials do not exist");
            return "redirect:/admin/home";
        }
    }

    @GetMapping("/adminTestPage")
    public String adminTestPage() {
        // Logic to fetch and display requests for admins
        return "admin/adminHome";
    }


    @GetMapping("/calendar")
    public String getCalendar(Model model) {
        return "admin/calendar";
    }

    @GetMapping("/viewRequests")
    public String viewRequests(Model model) {
        List<ExamSlotRequest> examSlotRequests = examRequestRepository.findAll();
        model.addAttribute("examSlotRequests", examSlotRequests);
        return "admin/viewRequests";
    }

    @PostMapping("/approveRequest")
    public String approveRequest(@RequestParam Map<String, String> examSlot) {

        int requestId = Integer.parseInt(examSlot.get("requestId"));


        ExamSlotRequest request = examRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("APPROVED");
            request.setPreferenceStatus(1);
            User owner = userRepository.findByUsername(request.getInstructorName());
            //-------------------------------------
            examSlot exam = new examSlot();
            exam.setStartTime(request.getExamDate());
            exam.setDuration(request.getExamDuration());

            exam.setNumOfRooms(Integer.parseInt(examSlot.get("numberOfRooms")));
            exam.setNumInvigilator(Integer.parseInt(examSlot.get("numberOfInvigilators")));
            exam.setAssignedRooms(examSlot.get("assignedRooms"));
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
        List<ExamSlotRequest> examSlotRequests = examRequestRepository.findByInstructorName(instructor.getUsername());
        instructor.setExamSlotRequests(examSlotRequests); // Assuming a setter method exists in User class
       // model.addAttribute("instructors", instructors);
    }

    model.addAttribute("instructors", instructors);
    return "admin/viewInstructors";
}
    


    @GetMapping("/viewInvigilators")
    public String viewInvigilators(Model model) {
        List<User> invigilators = userRepository.findByRoleName("INVIGILATOR");
        model.addAttribute("invigilators",invigilators);
        model.addAttribute("invigilatorService", invService);
        return "admin/viewInvigilators";
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
                return "admin/adminHome";

            } catch (RuntimeException err){
                throw new InstructorController.BadRequest(err.getMessage());
            }
        }
        return "redirect:/admin/home";
    }

    @PostMapping("/emailAll")
    public String emailAll(RedirectAttributes redirectAttributes) throws MessagingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {

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



