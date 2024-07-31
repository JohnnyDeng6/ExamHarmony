package ca.cmpt276.examharmony.Model.CourseSectionInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<CoursesSec, Integer> {
    CoursesSec findByCourseName(String courseName);
    List<CoursesSec> findAllByCourseName(String courseName);
}
