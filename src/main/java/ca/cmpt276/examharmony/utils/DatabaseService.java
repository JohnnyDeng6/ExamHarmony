package ca.cmpt276.examharmony.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE course_section CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE exam_request CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE exam_slot CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE instructor_courses CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE instructor_exam_requests CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE invigilator_request CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE course_conflict CASCADE");
    }
}
