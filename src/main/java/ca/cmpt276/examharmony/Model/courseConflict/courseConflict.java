package ca.cmpt276.examharmony.Model.courseConflict;
import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_conflict")
public class courseConflict {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    
    private String courseSec1;
   
    private String courseSec2;


    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCourseSec1() {
        return courseSec1;
    }

    public void setCourseSec1(String courseSec1) {
        this.courseSec1 = courseSec1;
    }

    public String getCourseSec2() {
        return courseSec2;
    }

    public void setCourseSec2(String courseSec2) {
        this.courseSec2 = courseSec2;
    }

}
