package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.RoleRepository;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginLogoutControllers {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @GetMapping("/login")
    public String login(){
        return "loginPage";
    }

    @GetMapping("/admin/home")
    public String adminTest(){
        return "adminTestPage";
    }

    // @GetMapping("/instructor/home")
    // public String instructorTest(){
    //     return "instructorTestPage";
    // }
     
<<<<<<< Updated upstream
    // @GetMapping("/invigilator/home")
    // public String invigilatorTest(){
    //     return "invigilatorTestPage";
    // }
   // For testing purposes
    @GetMapping("/test")    
=======
    @GetMapping("/invigilator/home")
    public String invigilatorTest(){
        return "invigilatorTestPage";
    }
    //For testing purposes
    @GetMapping("/test")
>>>>>>> Stashed changes
    public String test(){
        /*
        User newUser = new User();
        newUser.setEmailAddress("mdb543@sfu.ca");
        newUser.setName("Alex");
        newUser.setPassword(encoder.encode("4321"));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepo.findByName("INSTRUCTOR"));
        newUser.setRoles(roles);
        userRepo.save(newUser);
        */
        return "testPage";
    }
}

