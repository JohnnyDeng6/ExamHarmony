package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequest;
import ca.cmpt276.examharmony.Model.InvRequests.InvigilatorRequestService; 
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/invigilator")
public class InvigilatorController {
    private final InvigilatorRequestService invigilatorRequestService;

    @Autowired
    public InvigilatorController(InvigilatorRequestService invigilatorRequestService) {
        this.invigilatorRequestService = invigilatorRequestService;
    }

    @Autowired
    private IntervalRepository intervalRepository;

    @GetMapping("/home")
    public String showHomePage(Model model, Principal principal) {
        String username = principal.getName();
        List<InvigilatorRequest> requests = invigilatorRequestService.getRequests(username);
        EditInterval interval = intervalRepository.findById(1);
        model.addAttribute("interval", interval);
        model.addAttribute("invigilatorName", username);
        model.addAttribute("requests", requests);
        return "invigilator/invigilatorHome";
    }


    @GetMapping("/requests/{username}")
    @ResponseBody
    public List<InvigilatorRequest> getRequests(@PathVariable String username) {
        return invigilatorRequestService.getRequests(username);
    }

    @PostMapping("/requests/status")
    @ResponseBody
    public ResponseEntity<?> updateRequestStatus(@RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            String id = body.get("requestId");
            System.out.println(id);
            InvigilatorRequest updatedRequest = invigilatorRequestService.updateStatus(Integer.parseInt(id), status);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating request status: " + e.getMessage());
        }
    }
}
    