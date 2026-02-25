package com.sms.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file-based logger.
 * Writes timestamped log lines to data/app.log.
 * Personal student data is never logged – only IDs and action summaries.
 */
public class AppLogger {

    private static final String LOG_FILE = "data" + File.separator + "app.log";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {}

    public static void info(String message) {
        write("INFO ", message);
    }

    public static void warn(String message) {
        write("WARN ", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        new File("data").mkdirs();
        String line = "[" + LocalDateTime.now().format(FMT) + "] [" + level + "] " + message;

        // Also print to console so IntelliJ shows it
        System.out.println(line);

        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("Logger could not write to file: " + e.getMessage());
        }
    }
}
