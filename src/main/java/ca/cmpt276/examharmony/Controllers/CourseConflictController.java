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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class CourseConflictController {
    
    @Autowired
    private courseConflictRepository conflictRepo;

    @Autowired
    private CourseRepository courseRepo;

    @GetMapping("courseConflict/add")
    public String getAllCoursesSec(Model model){
        

        List<CoursesSec> courseSec = courseRepo.findAll();

        model.addAttribute("CoursesSec", courseSec);
        return "admin/courseConflictsAdd";
    }


    @GetMapping("/courseConflict")
    public String showAddConflictForm(Model model) {
        
        List<courseConflict> courseConflicts = conflictRepo.findAll();

        model.addAttribute("courseConflict", courseConflicts);
        return "admin/courseConflictsDisplay";
    }


    @PostMapping("/courseConflict/delete")
    public String deleteCourseConflict(@RequestParam("id") int id){

        if(conflictRepo.existsById(id)){
            System.out.println("delete successful");
            conflictRepo.deleteById(id);
        }
        return "redirect:/admin/courseConflict";
    }


    @PostMapping("/addCourseConflict")
    public String addCourseConflict(@RequestParam Map<String, String> newCourseConflict,RedirectAttributes redirectAttributes,HttpServletResponse response) throws Exception {
    
        String course1 = newCourseConflict.get("courseSec1");
        String course2 = newCourseConflict.get("courseSec2");

        System.out.println("course1 is: "+course1);
        System.out.println("course2 is: "+course2);
        

        boolean conflictExists = conflictRepo.existsConflict(course1,course2);

        if(!conflictExists){
            courseConflict conflict = new courseConflict();
            conflict.setCourseSec1(course1);
            conflict.setCourseSec2(course2);

            conflictRepo.save(conflict);
            return "redirect:/admin/courseConflict";
        }else{
            System.out.println("ERROR MESSAGE INSIDE");
            redirectAttributes.addFlashAttribute("errorMessage", "Conflict between these courses already exists.");
            return "redirect:/admin/courseConflict/add";
        }
        
    }

}
