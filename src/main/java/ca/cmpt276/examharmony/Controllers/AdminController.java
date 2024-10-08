package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;
import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.EditInterval.EditIntervalDTO;
import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequestService;
import ca.cmpt276.examharmony.Model.courseConflict.courseConflictRepository;
import ca.cmpt276.examharmony.utils.EmailService;
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
import ca.cmpt276.examharmony.utils.PdfService;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private courseConflictRepository conflictRepo;

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
    @Autowired
    private PdfService pdfService;

    @GetMapping("/generatePdf")
    public ResponseEntity<byte[]> generatePdf() throws IOException {
        byte[] pdfBytes = pdfService.generatePdf();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exam_slots.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

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

    public boolean overlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        
        LocalDate date1Start = start1.toLocalDate();
        LocalDate date1End = end1.toLocalDate();
        LocalDate date2Start = start2.toLocalDate();
        LocalDate date2End = end2.toLocalDate();
    
        
        boolean sameDay = date1Start.equals(date2Start) || date1Start.equals(date2End)
                          || date1End.equals(date2Start) || date1End.equals(date2End)
                          || (date2Start.isBefore(date1End) && date1Start.isBefore(date2End));
    
        
        return sameDay;
    }

    public static LocalDateTime calculateEndTime(LocalDateTime startTime, double durationInHours) {
        long hours = (long) durationInHours;
        long minutes = (long) ((durationInHours - hours) * 60);
        return startTime.plusHours(hours).plusMinutes(minutes);
    }


    @PostMapping("/approveRequest")
    public String approveRequest(@RequestParam Map<String, String> examSlot,RedirectAttributes redirectAttributes) {

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
        

            List<CoursesSec> CourseID = courseRepo.findAllByCourseName(request.getCourseName());
            exam.setCourseID(CourseID.get(0));
        
            examRepo.save(exam);


            String courseName = request.getCourseName();
            Double duration = request.getExamDuration();

            LocalDateTime StartTime= request.getExamDate();

            LocalDateTime EndTime = calculateEndTime(StartTime, duration);
    
            List<examSlot> examSlots = examRepo.findAll();
            
            for (examSlot exam1 : examSlots){
                LocalDateTime startTimeExist = exam1.getStartTime();
                LocalDateTime endTimeExist = calculateEndTime(startTimeExist,exam1.getDuration());
    
                if(overlap(StartTime,EndTime,startTimeExist,endTimeExist)){
                    String course1 = exam1.getCourseID().getCourseName();
                    String course2 = courseName;
    
                    boolean conflictExists = conflictRepo.existsConflict(course1,course2);
    
                    if(conflictExists){
                    
                        redirectAttributes.addFlashAttribute("errorMessage", "Exam slot added successfully, but a scheduling conflict was detected with the following course(s): "+course1 +" "+course2 +". Please review the schedule.");
                        break;
                    }
                }
    
            }

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



