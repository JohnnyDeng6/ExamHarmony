
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
import ca.cmpt276.examharmony.Model.courseConflict.courseConflict;
import ca.cmpt276.examharmony.Model.courseConflict.courseConflictRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import java.time.LocalDate;
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
    private courseConflictRepository conflictRepo;
    
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

    @PostMapping("/addExamSlot")
    public String addExamSlot(@RequestParam Map<String, String> newExamSlot,RedirectAttributes redirectAttributes, HttpServletResponse response) throws Exception {

        System.out.print("In addExamSlot");

        LocalDateTime StartTime = LocalDateTime.parse(newExamSlot.get("startTime"));
        double duration = Double.parseDouble(newExamSlot.get("duration"));
        int numOfRooms = Integer.parseInt(newExamSlot.get("numberOfRooms"));
        String assignedRooms = newExamSlot.get("assignedRooms");
        int numInvigilator = Integer.parseInt(newExamSlot.get("numberOfInvigilators"));

        String courseName = newExamSlot.get("courseID");

        LocalDateTime EndTime = calculateEndTime(StartTime, duration);

        List<examSlot> examSlots = examRepo.findAll();
        
        for (examSlot exam : examSlots){
            LocalDateTime startTimeExist = exam.getStartTime();
            LocalDateTime endTimeExist = calculateEndTime(startTimeExist,exam.getDuration());

            if(overlap(StartTime,EndTime,startTimeExist,endTimeExist)){
                String course1 = exam.getCourseID().getCourseName();
                String course2 = courseName;

                boolean conflictExists = conflictRepo.existsConflict(course1,course2);

                if(conflictExists){
                
                    redirectAttributes.addFlashAttribute("errorMessage", "Exam slot added successfully, but a scheduling conflict was detected with the following course(s): "+course1 +" "+course2 +". Please review the schedule.");
                    break;
                }
            }

        }



        String status = newExamSlot.get("status");
        
        examSlot exam = new examSlot();

        exam.setStartTime(StartTime);
        exam.setDuration(duration);
        exam.setNumOfRooms(numOfRooms);
        exam.setNumInvigilator(numInvigilator);
        exam.setAssignedRooms(assignedRooms);
        exam.setStatus(status);
        
        List<CoursesSec> CourseID = courseRepo.findAllByCourseName(courseName);
        exam.setCourseID(CourseID.get(0));
        
        examRepo.save(exam);
//
//        String calendarId = "52ea1e5d777a891982013dc26653684dcf6c30520e5526be1cb60c0c34c01d10@group.calendar.google.com";
//        String description = "Room number: " + numOfRooms + "\nRooms: " + assignedRooms + "\nNumber of Invigilators: " + numInvigilator + "\nStatus: " + status;
//
//        ZoneId zoneId = ZoneId.of("America/Vancouver");
//        ZonedDateTime zonedStartDateTime = StartTime.atZone(zoneId);
//        ZonedDateTime zonedEndDateTime = (StartTime.plusMinutes((long) (duration*60))).atZone(zoneId);
//
//        DateTime startDateTime = new DateTime(zonedStartDateTime.toInstant().toEpochMilli());
//        DateTime endDateTime = new DateTime(zonedEndDateTime.toInstant().toEpochMilli());
//
//        calendarManagementService.createEvent(calendarId, courseName, description, assignedRooms, startDateTime, endDateTime);
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
    public String updateExamSlot(@RequestParam Map<String, String> updatedExamSlot,RedirectAttributes redirectAttributes,HttpServletResponse response) throws Exception {

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


        List<CoursesSec> CourseID = courseRepo.findAllByCourseName(courseName);
        if (!CourseID.isEmpty()) {
            exam.setCourseID(CourseID.get(0));
        }

        String status = updatedExamSlot.get("status");
        exam.setStatus(status);

//            String calendarId = "52ea1e5d777a891982013dc26653684dcf6c30520e5526be1cb60c0c34c01d10@group.calendar.google.com";
//            String description = "Room number: " + numOfRooms + "\nRooms: " + assignedRooms + "\nNumber of Invigilators: " + numInvigilator + "\nStatus: " + status;
//
//            ZoneId zoneId = ZoneId.of("America/Vancouver");
//            ZonedDateTime zonedStartDateTime = StartTime.atZone(zoneId);
//            ZonedDateTime zonedEndDateTime = (StartTime.plusMinutes((long) (duration*60))).atZone(zoneId);
//
//            DateTime startDateTime = new DateTime(zonedStartDateTime.toInstant().toEpochMilli());
//            DateTime endDateTime = new DateTime(zonedEndDateTime.toInstant().toEpochMilli());
//
//            calendarManagementService.createEvent(calendarId, courseName, description, assignedRooms, startDateTime, endDateTime);




            List<examSlot> examSlots = examRepo.findAll();

            LocalDateTime EndTime = calculateEndTime(StartTime, duration);
            
            for (examSlot exam1 : examSlots){
                LocalDateTime startTimeExist = exam1.getStartTime();
                LocalDateTime endTimeExist = calculateEndTime(startTimeExist,exam1.getDuration());
    
                if(overlap(StartTime,EndTime,startTimeExist,endTimeExist)){
                    String course1 = exam1.getCourseID().getCourseName();
                    String course2 = courseName;
    
                    boolean conflictExists = conflictRepo.existsConflict(course1,course2);
    
                    if(conflictExists){
                    
                        redirectAttributes.addFlashAttribute("errorMessage", "Exam slot added successfully, but a scheduling conflict was detected with the following course(s): "+course1 +" "+course2 +". Please review the schedule.");
                    }
                }
    
            }
            examRepo.save(exam);

        return "redirect:/admin/examSlots";
    }

}