package ca.cmpt276.examharmony.calendarUtils;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class CalendarManagementService {

    private static Calendar getCalendarService() throws Exception {
        return GoogleCalendarService.getCalendarService();
    }

    public String createNewCalendar(String summary, String timeZone) throws Exception {
        Calendar service = getCalendarService();
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar()
                .setSummary(summary)
                .setTimeZone(timeZone);
        com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
        System.out.printf("Calendar created: %s\n", createdCalendar.getId());
        System.out.printf("Calendar id: %s\n", createdCalendar.getId());
        return createdCalendar.getId();
    }

    public void shareCalendarWithUsers(String calendarId, List<String> userEmails) throws Exception {
        Calendar service = getCalendarService();
        for (String email : userEmails) {
            if (!Objects.equals(email, "examharmony6@gmail.com")) {
                AclRule rule = new AclRule()
                        .setScope(new AclRule.Scope().setType("user").setValue(email))
                        .setRole("writer");
                AclRule createdRule = service.acl().insert(calendarId, rule).execute();
                System.out.printf("Calendar shared with %s: %s\n", email, createdRule.getId());
            }
        }
    }

    public void shareCalendarWithUser(String calendarId, String userEmail) throws Exception {
        Calendar service = getCalendarService();
            AclRule rule = new AclRule()
                    .setScope(new AclRule.Scope().setType("user").setValue(userEmail))
                    .setRole("writer");
            AclRule createdRule = service.acl().insert(calendarId, rule).execute();
            System.out.printf("Calendar shared with %s: %s\n", userEmail, createdRule.getId());
    }

    public void createEvent(String calendarId, String summary, String description, String location, DateTime startDateTime, DateTime endDateTime) throws Exception {
        Calendar service = getCalendarService();

        Event event = new Event()
                .setSummary(summary)
                .setDescription(description)
                .setLocation(location);

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Vancouver");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Vancouver");
        event.setEnd(end);

        Event createdEvent = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", createdEvent.getHtmlLink());

    }

    public String getCalendarLink(String calendarId) {
        return "https://calendar.google.com/calendar/embed?src=" + calendarId;
    }
}
