// ReminderProcessor.java
// Elena Phillips
// 10/28/23
// Process reminders

package edu.fscj.cop2805c.calendar;

import edu.fscj.cop2805c.log.Logger;
import edu.fscj.cop2805c.message.MessageProcessor;

// commented out all unused imports
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ReminderProcessor extends Thread
        implements MessageProcessor, Logger<Reminder> {

    private static final String LOGFILE = "remLog.txt";
    private ConcurrentLinkedQueue<Reminder> queue;
    private boolean stopped = false;

    public ReminderProcessor(ConcurrentLinkedQueue<Reminder> queue) {
        this.queue = queue;

        // start polling (invokes run(), below)
        this.start();
    }

    // remove messages from the queue and process them
    public void processMessages() {
        System.out.println("before processing, queue size is " + queue.size());
        queue.stream().forEach(e -> {
            // Do something with each element
            e = queue.remove();
            System.out.print(e);
            log(e);
        });
        System.out.println("after processing, queue size is now " + queue.size());
    }

    // allow external class to stop us
    public void endProcessing() {
        this.stopped = true;
        interrupt();
    }

    @Override
    public void log(Reminder c) {
        LocalDateTime local = LocalDateTime.from(
                Instant.now().atZone(ZoneId.systemDefault()));
        String msg = local.truncatedTo(ChronoUnit.MILLIS) +
                "greeting:" + c.getContact().getName();
        try (BufferedWriter remLog = Files.newBufferedWriter(Path.of(LOGFILE),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
            remLog.write(msg);
            remLog.newLine();
        } catch (IOException e) {
            System.out.println("LOG FAIL" + msg);
            e.printStackTrace();
        }

    }

    // poll queue for cards
    public void run() {
        final int SLEEP_TIME = 1000; // ms
        while (true) {
            try {
                processMessages();
                Thread.sleep(SLEEP_TIME);
                System.out.println("polling");
            } catch (InterruptedException ie) {
                // see if we should exit
                if (this.stopped == true) {
                    System.out.println("poll thread received exit signal");
                    break;
                }
            }
        }
    }
}

