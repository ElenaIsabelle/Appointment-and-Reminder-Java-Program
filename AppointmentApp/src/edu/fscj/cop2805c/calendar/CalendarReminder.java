// CalendarReminder.java
// Elena Phillips
// 10/28/2023
// Interface which supports building and sending reminders

package edu.fscj.cop2805c.calendar;

public interface CalendarReminder {
    // build a reminder in the form of a formatted String
    public Reminder buildReminder(Appointment appt);

    // send a reminder using contact's preferred notification method
    public void sendReminder(Reminder reminder);
}
