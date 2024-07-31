package ca.cmpt276.examharmony.Model.courseConflict;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface courseConflictRepository extends JpaRepository<courseConflict, Integer> {
    @Query("SELECT COUNT(c) > 0 FROM courseConflict c WHERE " +
           "(c.courseSec1 = :courseSec1 AND c.courseSec2 = :courseSec2) OR " +
           "(c.courseSec1 = :courseSec2 AND c.courseSec2 = :courseSec1)")
    boolean existsConflict(@Param("courseSec1") String courseSec1, @Param("courseSec2") String courseSec2);
}
