package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import ca.cmpt276.examharmony.utils.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginLogoutControllers {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private IntervalRepository intervalRepository;

    @GetMapping("/login")
    public String login(){
        return "general/loginPage";
    }



    @GetMapping("/test")    
    public String test(){
        return "general/testPage";
    }
}

