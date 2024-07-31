package ca.cmpt276.examharmony.Model.examSlot;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.cmpt276.examharmony.Model.CourseSectionInfo.CoursesSec;

import java.util.List;

public interface examSlotRepository extends JpaRepository<examSlot, Integer> {
    
    
}
