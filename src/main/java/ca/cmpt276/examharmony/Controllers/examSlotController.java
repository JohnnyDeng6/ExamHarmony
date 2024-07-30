
package ca.cmpt276.examharmony.Controllers;
import ca.cmpt276.examharmony.calendarUtils.CalendarManagementService;
import com.google.api.client.util.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import ca.cmpt276.examharmony.Model.examSlot.examSlotRepository;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;
import jakarta.servlet.http.HttpServletResponse;
import ca.cmpt276.examharmony.Model.examSlot.examSlot;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class examSlotController {
    
    @Autowired
    private examSlotRepository examRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private CalendarManagementService calendarManagementService;

    @GetMapping("examSlot/Add")
    public String getAllCoursesSec(Model model){
        System.out.println("examSlotAdd called");

        List<CoursesSec> courseSec = courseRepo.findAll();

        model.addAttribute("CoursesSec", courseSec);
        return "adminExamSlot";
    }


    @GetMapping("/admin/examSlots")
    public String getAllExamSlots(Model model){
        List<examSlot> examSlot = examRepo.findAll();
        model.addAttribute("examSlots", examSlot);
        return "/adminExamDisplay";
    }

    @PostMapping("/admin/addExamSlot")
    public String addExamSlot(@RequestParam Map<String, String> newExamSlot, HttpServletResponse response) throws Exception {

        System.out.print("In addExamSlot");

        LocalDateTime StartTime = LocalDateTime.parse(newExamSlot.get("startTime"));
        double duration = Double.parseDouble(newExamSlot.get("duration"));
        int numOfRooms = Integer.parseInt(newExamSlot.get("numberOfRooms"));
        String assignedRooms = newExamSlot.get("assignedRooms");
        int numInvigilator = Integer.parseInt(newExamSlot.get("numberOfInvigilators"));

        String courseName = newExamSlot.get("courseID");
        
        System.out.print("courseName is "+courseName);

        String status = newExamSlot.get("status");
        
        examSlot exam = new examSlot();

        exam.setStartTime(StartTime);
        exam.setDuration(duration);
        exam.setNumOfRooms(numOfRooms);
        exam.setNumInvigilator(numInvigilator);
        exam.setAssignedRooms(assignedRooms);
        // exam.setAdmin(admin);
        exam.setStatus(status);
        
        CoursesSec CourseID = courseRepo.findByCourseName(courseName);
        exam.setCourseID(CourseID);
        
        examRepo.save(exam);

        String calendarId = "52ea1e5d777a891982013dc26653684dcf6c30520e5526be1cb60c0c34c01d10@group.calendar.google.com";
        String summary = "Room number: " + numOfRooms + "\nRooms: " + assignedRooms + "\nNumber of Invigilators: " + numInvigilator;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        ZoneId zoneId = ZoneId.of("America/Vancouver");
        ZonedDateTime zonedStartDateTime = StartTime.atZone(zoneId);
        ZonedDateTime zonedEndDateTime = (StartTime.plusHours((long) duration)).atZone(zoneId);

        DateTime startDateTime = new DateTime(zonedStartDateTime.toInstant().toEpochMilli());
        DateTime endDateTime = new DateTime(zonedEndDateTime.toInstant().toEpochMilli());

        calendarManagementService.createEvent(calendarId, summary, courseName, assignedRooms, startDateTime, endDateTime);
        response.setStatus(HttpServletResponse.SC_CREATED);
        
        return "redirect:/admin/examSlots";
    }
    
    @PostMapping("/examSlot/delete")
    public String deleteExamSlot(@RequestParam("id") int id){

        if(examRepo.existsById(id)){
            System.out.println("delete successful");
            examRepo.deleteById(id);
        }
        return "redirect:/admin/examSlots";
    }

    @GetMapping("/examSlot/select")
    public String selectExamSlot(@RequestParam("id") int id, Model model){
        System.out.print("SELECTION PRINT");

        examSlot exam = examRepo.findById(id).orElse(null);
        if(exam != null){
            model.addAttribute("examSlot",exam);
            List<CoursesSec> courseSec = courseRepo.findAll();
            model.addAttribute("CoursesSec", courseSec);

            return "/updateExamSlot";
        }
        return "redirect:/admin/examSlots";
    }


    @PostMapping("examSlot/update")
    public String updateExamSlot(@RequestParam Map<String, String> updatedExamSlot,HttpServletResponse response){
        examSlot exam = examRepo.findById(Integer.parseInt(updatedExamSlot.get("id"))).orElseThrow(()-> new IllegalArgumentException("Invalid exam slot ID"));
        
        
    
        if (updatedExamSlot.containsKey("startTime") && !updatedExamSlot.get("startTime").isEmpty()) {
            LocalDateTime StartTime = LocalDateTime.parse(updatedExamSlot.get("startTime"));
            exam.setStartTime(StartTime);
        }
    
        if (updatedExamSlot.containsKey("duration") && !updatedExamSlot.get("duration").isEmpty()) {
            double duration = Double.parseDouble(updatedExamSlot.get("duration"));
            exam.setDuration(duration);
        }
    
        if (updatedExamSlot.containsKey("numOfRooms") && !updatedExamSlot.get("numOfRooms").isEmpty()) {
            int numOfRooms = Integer.parseInt(updatedExamSlot.get("numOfRooms"));
            exam.setNumOfRooms(numOfRooms);
        }
    
        if (updatedExamSlot.containsKey("assignedRooms") && !updatedExamSlot.get("assignedRooms").isEmpty()) {
            String assignedRooms = updatedExamSlot.get("assignedRooms");
            exam.setAssignedRooms(assignedRooms);
        }
    
        if (updatedExamSlot.containsKey("numInvigilator") && !updatedExamSlot.get("numInvigilator").isEmpty()) {
            int numInvigilator = Integer.parseInt(updatedExamSlot.get("numInvigilator"));
            exam.setNumInvigilator(numInvigilator);
        }

        if(updatedExamSlot.containsKey("courseName") && !updatedExamSlot.get("courseName").isEmpty()){
            
            String courseName = updatedExamSlot.get("courseName");
            
            CoursesSec CourseID = courseRepo.findByCourseName(courseName);
            exam.setCourseID(CourseID);
        }
    
        if (updatedExamSlot.containsKey("status") && !updatedExamSlot.get("status").isEmpty()) {
            String status = updatedExamSlot.get("status");
            exam.setStatus(status);
        }
        

        examRepo.save(exam);
        
        return "redirect:/admin/examSlots";
    }

}