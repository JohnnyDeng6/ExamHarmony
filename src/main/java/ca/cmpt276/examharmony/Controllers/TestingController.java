package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;
import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.roles.Role;
import ca.cmpt276.examharmony.Model.roles.RoleRepository;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;
import java.util.Set;

@Controller
public class TestingController {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder pwEncorder;

    @Autowired
    private IntervalRepository intervalRepository;

    @GetMapping("/adminExamSlot")
    public String showAdminExamSlotPage() {
        return "adminExamSlot"; // This returns the template named "adminExamSlot.html"
    }

    @GetMapping("/adminExamDisplay")
    public String showAdminExamDisplayPage(){
        return "/adminExamDisplay";
    }

    @GetMapping("/invTest")
    public String showInvigilator() {
        return "invigilatorTestPage"; // This returns the template named "adminExamSlot.html"
    }

    @GetMapping("/test/add_test_course")
    public void addTestCourse(){

        CoursesSec newCourse = new CoursesSec();
        newCourse.setCourseName("CMPT354");
        newCourse.setDepartment("CMPT");
        newCourse.setExamSlotStatus(false);
        courseRepo.save(newCourse);

    }

    // @GetMapping("/test")
    // public String getTestPage(){
    //     return "testPage";
    // }

    @GetMapping("/test/add_user_with_course")
    public void addTestInstructor(){
        CoursesSec course = courseRepo.findByCourseName("CMPT354");
        String password = pwEncorder.encode("Helios6789!");
        User newUser = User.createUser("Jackson", password , "JacksonPollack@lazersharks.com", "Jackson");
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(roleRepo.findByName("INSTRUCTOR"));
        newUser.addCourse(course);
        newUser.setRoles(roleSet);
        userRepo.save(newUser);
    }

    @GetMapping("/test/add/interval")
    public void addEditInterval(){
        EditInterval test = new EditInterval();
        test.setStartTime("2024-06-15 13:00");
        test.setEndTime("2028-06-15 13:00");
        intervalRepository.save(test);
    }

}