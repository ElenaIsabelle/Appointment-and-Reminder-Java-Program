// AppointmentApp.java
// Elena Phillips
// 10/28/2023
// creates an appointment for a contact

package edu.fscj.cop2805c.calendar;

import edu.fscj.cop2805c.dispatch.Dispatcher;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

// main application class
public class AppointmentApp implements CalendarReminder, Dispatcher<Reminder> {
    private ArrayList<Appointment> appointments = new ArrayList<>();

    private static final String CONTACT_FILE = "contact.dat";

    // Use a safe queue to act as message queue for the dispatcher
    private ConcurrentLinkedQueue<Reminder> queue =
            new ConcurrentLinkedQueue<>(new LinkedList<>());
    private Random rand = new Random();
    private int numAppointments = 0;

    // dispatch the reminder using the dispatcher
    public void dispatch(Reminder reminder) {
        this.queue.add(reminder);
    }

    // build a reminder in the form of a formatted String
    public Reminder buildReminder(Appointment appt) {

        final String NEWLINE = "\n";

        Contact c = appt.getContact();

        // build the reminder message
        // embed newlines so we can split per line and use token (line) lengths
        String msg = "";
        try {
            // load the property and create the localized greeting
            ResourceBundle res = ResourceBundle.getBundle(
                    "edu.fscj.cop2805c.calendar.Reminder", c.getLocale());
            String youHaveAnAppointment = res.getString("YouHaveAnAppointment");

            // format and display the date
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
            formatter =
                    formatter.localizedBy(c.getLocale());
            msg = appt.getReminder().format(formatter) + NEWLINE;

            // add the localized reminder
            msg += youHaveAnAppointment + "\n" + c.getName() + NEWLINE;
        } catch (java.util.MissingResourceException e) {
            System.err.println(e);
            msg = "You Have An Appointment!" + NEWLINE + c.getName() + NEWLINE;
        }

        msg +=
                "Title: " + appt.getTitle() + NEWLINE +
                        "Description: " + appt.getDescription() + NEWLINE;
        // split and get the max length
        String[] msgSplit = msg.split(NEWLINE);
        int maxLen = 0;
        for (String s : msgSplit)
              if (s.length() > maxLen)
                  maxLen = s.length();
        maxLen += 4; // Adjust for padding and new line

        // create our header/footer (all plus signs)
        char[] plusChars = new char[maxLen];
        Arrays.fill(plusChars, '+');
        String headerFooter = new String(plusChars);

        // add the header to our output
        String newMsg = headerFooter + "\n";

        // reuse the header template for our body lines (plus/spaces/plus)
        Arrays.fill(plusChars, ' ');
        plusChars[0] = plusChars[maxLen - 1] = '+';
        String bodyLine = new String(plusChars);

        // for each string in the output, insert into a body line
        for (String s : msgSplit) {
            StringBuilder sBld = new StringBuilder(bodyLine);
            // add 2 to end position in body line replace
            // operation so final space/plus don't get pushed out
            sBld.replace(2,s.length() + 2, s);
            // add to our output
            newMsg += new String(sBld) + "\n";
        }
        newMsg += headerFooter + "\n";

        Reminder newReminder = new Reminder(newMsg, appt.getReminder(), c);
        return newReminder;
    }

    // send a reminder using contact's preferred notification method
    public void sendReminder(Reminder reminder) {
        Dispatcher<Reminder> d = (c) -> {this.queue.add(c);};
        dispatch(reminder);
    }

    private Appointment createRandomAppointment(Contact c) {
        ZonedDateTime apptTime, reminder;
        int plusVal = rand.nextInt() % 12 + 1;
        // create a future appointment using random month value
        apptTime = ZonedDateTime.now().plusMonths(plusVal);

        // create the appt reminder for the appointment time minus random (<24) hours
        // use absolute value in case random is negative to prevent reminders > appt
        int minusVal = Math.abs(rand.nextInt()) % 24 + 1;
        reminder = apptTime.minusHours(minusVal);
        // create an appointment using the contact and appt time
        int apptNum = appointments.size() + 1;
        Appointment appt = new Appointment("Test Appointment " + ++numAppointments,
                "This is test appointment " + numAppointments,
                c, apptTime);
        appt.setReminder(reminder);
        return appt;
    }

    private void addAppointments(Appointment... appointmentList) {
        appointments.addAll(Arrays.asList(appointmentList));
    }

    private void checkReminderTime(Appointment appt) {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime dt = appt.getReminder();

        // see if it's time to send a reminder
        // TODO: create a Reminder class and override equals()
        if (    dt.getYear() == current.getYear() &&
                dt.getMonth() == current.getMonth() &&
                dt.getDayOfMonth() == current.getDayOfMonth() &&
                dt.getHour() == current.getHour() &&
                dt.getMinute() == current.getMinute()) {
            Reminder reminder = buildReminder(appt);
            sendReminder(reminder);
        }

    }

    public void writeAppointment(ArrayList<Appointment> al) {
        try (ObjectOutputStream appointmentData = new ObjectOutputStream(
                new FileOutputStream(CONTACT_FILE));) {
            appointmentData.writeObject(al);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Appointment> readAppointment() {
        ArrayList<Appointment> list = new ArrayList<>();

        try (ObjectInputStream appointmentData = new ObjectInputStream(new FileInputStream(CONTACT_FILE));) {
            list = (ArrayList<Appointment>) (appointmentData.readObject());
                for (Appointment a : list) {
                    System.out.println("read contacts: read " + a.getContact().getName());
                }
        } catch (FileNotFoundException e) {
            System.out.println("read contacts: no input file.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    // unit test
    public static void main(String[] args) {

        AppointmentApp apptApp = new AppointmentApp();
        ArrayList<Appointment> appointmentList = apptApp.readAppointment();
        ReminderProcessor processor = new ReminderProcessor(apptApp.queue);
        ZonedDateTime current = ZonedDateTime.now();

        if (appointmentList.isEmpty()) {
            // start with a contact
            Contact c = new Contact("Smith", "John", "JohnSmith@email.com",
                    "(904) 555-1212", ReminderPreference.PHONE,
                    ZoneId.of("America/New_York"), new Locale("en"));
            Appointment a1 = apptApp.createRandomAppointment(c);
            a1.setReminder(current);

            // create more contacts to test locales
            c = new Contact("Coutaz", "Joëlle", "Joëlle.Coutaz@email.com",
                    "33 01 09 75 83 51", ReminderPreference.EMAIL,
                    ZoneId.of("Europe/Paris"), new Locale("fr"));
            Appointment a2 = apptApp.createRandomAppointment(c);
            a2.setReminder(current);

            c = new Contact("Bechtolsheim", "Andy", "Andy.Bechtolsheim@email.com",
                    "33 01 09 75 83 51", ReminderPreference.EMAIL,
                    ZoneId.of("Europe/Berlin"), new Locale("de"));
            Appointment a3 = apptApp.createRandomAppointment(c);
            a3.setReminder(current);

            c = new Contact("Peisu", "Xia", "Xia.Peisu@email.com",
                    "33 01 09 75 83 51", ReminderPreference.EMAIL,
                    ZoneId.of("Asia/Shanghai"), new Locale("zh"));
            Appointment a4 = apptApp.createRandomAppointment(c);
            a4.setReminder(current);

            apptApp.addAppointments(a1, a2, a3, a4);

            // send reminders where needed
            for (Appointment a : apptApp.appointments)
                apptApp.checkReminderTime(a);

            // wait for a bit
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                System.out.println("sleep interrupted! " + ie);
            }
        }
        else {
            System.out.println("contacts were read from data file.");
        }

        // done
        processor.endProcessing();

        apptApp.writeAppointment(appointmentList);

    }
}

