// Reminder.java
// Elena Phillips
// 10/28/2023
// Class which represents a reminder for an appointment

package edu.fscj.cop2805c.calendar;

import java.time.ZonedDateTime;

public class Reminder {

    private String message;
    private ZonedDateTime dateTime;
    Contact contact;

    public Reminder() { }

    public Reminder(String message, ZonedDateTime dateTime, Contact contact) {
        this.message = message;
        this.dateTime = dateTime;
        this.contact = contact;
    }

    public String getMessage() {
        return message;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public Contact getContact() {
        return contact;
    }

    @Override public String toString() {
        return message;
    }
}
