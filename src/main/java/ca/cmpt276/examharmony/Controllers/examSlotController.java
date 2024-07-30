
package ca.cmpt276.examharmony.Controllers;
import ca.cmpt276.examharmony.calendarUtils.CalendarManagementService;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import org.hibernate.sql.ast.tree.expression.Star;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import ca.cmpt276.examharmony.Model.examSlot.examSlotRepository;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;
import jakarta.servlet.http.HttpServletResponse;
import ca.cmpt276.examharmony.Model.examSlot.examSlot;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;

import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class examSlotController {
    
    @Autowired
    private examSlotRepository examRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private CalendarManagementService calendarManagementService;

    @GetMapping("examSlot/add")
    public String getAllCoursesSec(Model model){
        System.out.println("examSlotAdd called");

        List<CoursesSec> courseSec = courseRepo.findAll();

        model.addAttribute("CoursesSec", courseSec);
        return "admin/adminExamSlot";
    }


    @GetMapping("/examSlots")
    public String getAllExamSlots(Model model){
        List<examSlot> examSlot = examRepo.findAll();
        model.addAttribute("examSlots", examSlot);
        return "admin/adminExamDisplay";
    }

    @PostMapping("/addExamSlot")
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
        exam.setStatus(status);
        
        CoursesSec CourseID = courseRepo.findByCourseName(courseName);
        exam.setCourseID(CourseID);
        
        examRepo.save(exam);

        String calendarId = "52ea1e5d777a891982013dc26653684dcf6c30520e5526be1cb60c0c34c01d10@group.calendar.google.com";
        String description = "Room number: " + numOfRooms + "\nRooms: " + assignedRooms + "\nNumber of Invigilators: " + numInvigilator + "\nStatus: " + status;

        ZoneId zoneId = ZoneId.of("America/Vancouver");
        ZonedDateTime zonedStartDateTime = StartTime.atZone(zoneId);
        ZonedDateTime zonedEndDateTime = (StartTime.plusMinutes((long) (duration*60))).atZone(zoneId);

        DateTime startDateTime = new DateTime(zonedStartDateTime.toInstant().toEpochMilli());
        DateTime endDateTime = new DateTime(zonedEndDateTime.toInstant().toEpochMilli());

        calendarManagementService.createEvent(calendarId, courseName, description, assignedRooms, startDateTime, endDateTime);
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

            return "admin/updateExamSlot";
        }
        return "redirect:/admin/examSlots";
    }


    @PostMapping("examSlot/update")
    public String updateExamSlot(@RequestParam Map<String, String> updatedExamSlot,HttpServletResponse response) throws Exception {

        try {
            examSlot exam = examRepo.findById(Integer.parseInt(updatedExamSlot.get("id"))).orElseThrow(()-> new IllegalArgumentException("Invalid exam slot ID"));
            LocalDateTime StartTime = LocalDateTime.parse(updatedExamSlot.get("startTime"));
            exam.setStartTime(StartTime);

            double duration = Double.parseDouble(updatedExamSlot.get("duration"));
            exam.setDuration(duration);

            int numOfRooms = Integer.parseInt(updatedExamSlot.get("numOfRooms"));
            exam.setNumOfRooms(numOfRooms);

            String assignedRooms = updatedExamSlot.get("assignedRooms");
            exam.setAssignedRooms(assignedRooms);

            int numInvigilator = Integer.parseInt(updatedExamSlot.get("numInvigilator"));
            exam.setNumInvigilator(numInvigilator);


            String courseName = updatedExamSlot.get("courseName");

            CoursesSec CourseID = courseRepo.findByCourseName(courseName);
            exam.setCourseID(CourseID);

            String status = updatedExamSlot.get("status");
            exam.setStatus(status);

            String calendarId = "52ea1e5d777a891982013dc26653684dcf6c30520e5526be1cb60c0c34c01d10@group.calendar.google.com";
            String description = "Room number: " + numOfRooms + "\nRooms: " + assignedRooms + "\nNumber of Invigilators: " + numInvigilator + "\nStatus: " + status;

            ZoneId zoneId = ZoneId.of("America/Vancouver");
            ZonedDateTime zonedStartDateTime = StartTime.atZone(zoneId);
            ZonedDateTime zonedEndDateTime = (StartTime.plusMinutes((long) (duration*60))).atZone(zoneId);

            DateTime startDateTime = new DateTime(zonedStartDateTime.toInstant().toEpochMilli());
            DateTime endDateTime = new DateTime(zonedEndDateTime.toInstant().toEpochMilli());

            calendarManagementService.createEvent(calendarId, courseName, description, assignedRooms, startDateTime, endDateTime);


            examRepo.save(exam);

        }catch (GoogleJsonResponseException e) {
            System.err.println("API Error: " + e.getDetails());
            return "redirect:/admin/home";
        }

        return "redirect:/admin/examSlots";
    }

}