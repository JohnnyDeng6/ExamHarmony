package ca.cmpt276.examharmony.Controllers;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CourseRepository;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSecDTO;
import ca.cmpt276.examharmony.Model.EditInterval.EditInterval;
import ca.cmpt276.examharmony.Model.EditInterval.IntervalRepository;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequest;
import ca.cmpt276.examharmony.utils.CustomUserDetails;
import ca.cmpt276.examharmony.Model.DTOs.DepartmentDTO;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestDTO;
import ca.cmpt276.examharmony.Model.examRequest.ExamSlotRequestRepository;
import ca.cmpt276.examharmony.Model.user.User;
import ca.cmpt276.examharmony.Model.user.UserRepository;
import ca.cmpt276.examharmony.utils.InstructorExamSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.Collections.sort;

@Controller
public class InstructorController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class BadRequest extends RuntimeException{
        public BadRequest(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException{
        public NotFoundException(String message) {
            super(message);
        }
    }

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private ExamSlotRequestRepository requestRepo;

    @Autowired
    private InstructorExamSlotRepository instructorExamSlotRepo;

    @Autowired
    private IntervalRepository intervalRepo;

    private List<DepartmentDTO> departments = Collections.synchronizedList(new ArrayList<>());

    @GetMapping("/instructor/home")
    public String InstructorInfo(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User instructor = userRepo.findByUsername(userDetails.getUsername());
            model.addAttribute("instructor", instructor);
            return "instructor/instructorHome";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/instructor/examslots/{courseName}")
    public String InstructorRequests(Model model, @PathVariable("courseName") String courseName) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
        User instructor = userRepo.findByUsername(userDetails.getUsername());
        List<ExamSlotRequest> examSlotRequests = requestRepo.findExamRequestsByCourseName(courseName);
        EditInterval editTime = intervalRepo.findById(1);

        model.addAttribute("interval", editTime);
        model.addAttribute("examRequests", examSlotRequests);
        model.addAttribute("instructor", instructor);
        model.addAttribute("courseName", courseName);
        return "instructor/viewExamSlotRequests";
    } else {
        return "redirect:/login";
    }
}


    @PostMapping("/instructor/examslots/edit/{courseName}")
    public String submitData(@RequestBody List<ExamSlotRequestDTO> examSlotRequestDTOList, Model model, @PathVariable("courseName") String courseName) {
        if (examSlotRequestDTOList.isEmpty()) {
            return "redirect:/instructor/examslots/" + courseName;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return "redirect:/login";
        }

        User instructor = userRepo.findByUsername(userDetails.getUsername());
        if (instructor == null) {
            return "redirect:/login";
        }

        List<ExamSlotRequest> previousRequests = requestRepo.findExamRequestsByCourseName(courseName);

        sort(examSlotRequestDTOList);
        sort(previousRequests);
        Iterator<ExamSlotRequest> examRequestIterator = previousRequests.iterator();
        int preferenceStatus = 1;

        // Update already-existing exam requests
       while(examRequestIterator.hasNext()) {
           ExamSlotRequest previousRequest = examRequestIterator.next();

            //Find a new request which has the same preference and update the old request
            Iterator<ExamSlotRequestDTO> iterator = examSlotRequestDTOList.iterator();

            while (iterator.hasNext()){
                ExamSlotRequestDTO newRequest = iterator.next();
                if(newRequest.preferenceStatus == previousRequest.getPreferenceStatus()){
                    try{
//                        previousRequest.setExamCode(newRequest.examCode);
                        previousRequest.setExamDuration(newRequest.examDuration);
                        previousRequest.setExamDate(newRequest.examDate);
                        previousRequest.setInstructorName(newRequest.instructorName);
                        instructor.updateExamRequest(previousRequest, newRequest.examDate);
                        requestRepo.save(previousRequest);
                        iterator.remove();
                    } catch (RuntimeException invalidParameter){
                        throw new BadRequest(invalidParameter.getMessage());
                    }
                }
            }
            preferenceStatus++;
       }
       // Add new exam requests
        for (ExamSlotRequestDTO newRequestDTO : examSlotRequestDTOList) {
            try {
                ExamSlotRequest newRequest = new ExamSlotRequest();
//                newRequest.setExamCode(newRequestDTO.examCode);
                newRequest.setExamDuration(newRequestDTO.examDuration);
                newRequest.setExamDate(newRequestDTO.examDate);
                newRequest.setCourseName(courseName);
                newRequest.setStatus("PENDING");
                newRequest.setPreferenceStatus(preferenceStatus);
                newRequest.setInstructorName(newRequestDTO.instructorName);
                requestRepo.save(newRequest);
                instructor.addNewExamRequest(newRequest);
                preferenceStatus++;
            } catch (RuntimeException invalidParameter) {
                throw new BadRequest(invalidParameter.getMessage());
            }
        }
        userRepo.save(instructor);

        EditInterval editTime = intervalRepo.findById(1);
        model.addAttribute("interval", editTime);
        model.addAttribute("examRequests", instructor.findRequestsByCourse(courseName));
        model.addAttribute("instructor", instructor);
        model.addAttribute("courseName", courseName);
        return "instructor/viewExamSlotRequests";

    }

    @DeleteMapping("/instructor/examslots/delete/{courseName}/{preference}")
    public String deleteRequest(Model model, @PathVariable("courseName") String courseName
            , @PathVariable("preference") int preference){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            User instructor = userRepo.findByUsername(userDetails.getUsername());
            List<ExamSlotRequest> examSlotRequestList = instructor.findRequestsByCourse(courseName);
            Iterator<ExamSlotRequest> iterator = examSlotRequestList.iterator();
            //Find request to delete
            while (iterator.hasNext()) {
                ExamSlotRequest request = iterator.next();
                if (request.getPreferenceStatus() == preference) {
                    instructorExamSlotRepo.removeUserExamRequestAssociation(instructor.getUuid(), request.getID());
                    instructor.deleteExamRequest(request);
                    requestRepo.delete(request);
                    iterator.remove();
                    //Fix all succeeding request's preference status
                    while(iterator.hasNext()){
                        ExamSlotRequest nextRequest = iterator.next();
                        nextRequest.setPreferenceStatus(nextRequest.getPreferenceStatus()-1);
                        requestRepo.save(nextRequest);
                    }
                    EditInterval editTime = intervalRepo.findById(1);

                    model.addAttribute("interval", editTime);
                    model.addAttribute("examRequests", instructor.findRequestsByCourse(courseName));
                    model.addAttribute("instructor", instructor);
                    model.addAttribute("courseName", courseName);
                    return "instructor/viewExamSlotRequests";
                }
            }
            throw new NotFoundException("Requested exam slot request does not exist");
        } else {
            return "redirect:/login";
        }
    }


    @PostMapping("/instructor/get/departments")
    public String getDepartments(Model model, @RequestBody List<DepartmentDTO> departments) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            this.departments.clear();
            this.departments.addAll(departments);
            model.addAttribute("departmentInfo", this.departments);
            return "instructor/department-selection";
        }
        return "redirect:/login";
    }

    @GetMapping("/instructor/view/departments/{semester}")
    public String viewDepartments(Model model, @PathVariable("semester") String semester){
        model.addAttribute("departmentInfo", this.departments);
        model.addAttribute("semester", semester);
        return "instructor/department-selection";
    }

    @PostMapping("/instructor/view/add/course")
    public String addCourse(Model model, @RequestBody CoursesSecDTO newCourse){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            User currentUser = userRepo.findByUsername(userDetails.getUsername());
            CoursesSec course = CoursesSec.CreateNewCourse(newCourse.department, newCourse.courseName);

            System.out.println(currentUser);

            Set<CoursesSec> instructorCourses = currentUser.getInstructorCourses();
            for(CoursesSec currentCourse: instructorCourses){
                if(currentCourse.getCourseName().equals(course.getCourseName())){
                    throw new BadRequest("You are already teaching this Course");
                }
            }

//            List<User> instructors = userRepo.findByRoleName("INSTRUCTOR");
//
//            for(User user : instructors){
//                Set<CoursesSec> courses = user.getInstructorCourses();
//                for(CoursesSec courseSec : courses){
//                    if(courseSec.getCourseName().equals(course.getCourseName())){
//                        throw new BadRequest("Another instructor is teaching this course");
//                    }
//                }
//            }

            courseRepo.save(course);
            currentUser.addCourse(course);
            userRepo.save(currentUser);
            model.addAttribute("instructor", currentUser);
            return "instructor/instructorHome";
        } else {
            return "redirect:/login";
        }
    }
}

