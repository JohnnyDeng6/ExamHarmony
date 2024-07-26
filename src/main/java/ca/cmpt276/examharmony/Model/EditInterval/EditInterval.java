package ca.cmpt276.examharmony.Model.EditInterval;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "edit_interval")
public class EditInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime newDate = LocalDateTime.parse(startTime, formatter);

        if(newDate.equals(endTime)){
            throw new RuntimeException("Start and end times cannot be equal");
        }

        if(newDate.isAfter(endTime)){
            throw new RuntimeException("Invalid start date");
        }

        try{

            this.startTime = LocalDateTime.parse(startTime, formatter);
        } catch (DateTimeParseException err){
            throw new RuntimeException("Date could not be parsed, check format and make sure it contains valid characters");
        }

    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime newDate = LocalDateTime.parse(endTime, formatter);

        if(newDate.equals(this.startTime)){
            throw new RuntimeException("Start and end times cannot be equal");
        }

        if(newDate.isBefore(startTime)){
            throw new RuntimeException("Invalid end date");
        }

        try{
            this.endTime = LocalDateTime.parse(endTime, formatter);

        } catch (DateTimeParseException err){
            throw new RuntimeException("Date could not be parsed, check format and make sure it contains valid characters");
        }
    }
}