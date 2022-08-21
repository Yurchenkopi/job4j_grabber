package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit  {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        try (Connection cn = initConnection(new Properties())) {
            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            Properties pr = new Properties();
            loadProperties(pr, "rabbit.properties");
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(pr.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static void loadProperties(Properties properties, String filename) {
        ClassLoader loader = AlertRabbit.class.getClassLoader();
        try (InputStream in = loader.getResourceAsStream(filename)) {
            properties.load(in);
            if (properties.getProperty("rabbit.interval") == null) {
                throw new NullPointerException("Cannot find \"rabbit interval\" in the properties file.");
            }
            String validData = properties.getProperty("rabbit.interval");
            if (validData.isEmpty()) {
                throw new NumberFormatException("The file parameter \"rabbit interval\" is empty.");
            }
            Scanner scanNum = new Scanner(validData);
            if (!scanNum.hasNextInt()) {
                throw new IllegalArgumentException("The rabbit interval should be a number.");
            }
            if (scanNum.nextInt() <= 0) {
                throw new IllegalArgumentException("The rabbit interval number should be greater than zero.");
            }
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Connection initConnection(Properties config) throws Exception {
        ClassLoader loader = Rabbit.class.getClassLoader();
        try (InputStream in = loader.getResourceAsStream("rabbit.properties")) {
            config.load(in);
            Class.forName(config.getProperty("grabber.driver"));
            return DriverManager.getConnection(
                    config.getProperty("grabber.url"),
                    config.getProperty("grabber.username"),
                    config.getProperty("grabber.password")
            );
        }
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            LocalDateTime created = LocalDateTime.now();
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (var ps = cn.prepareStatement(
                    "INSERT INTO rabbit(created_date) VALUES (?);")) {
                ps.setTimestamp(1, Timestamp.valueOf(created.format(FORMATTER)));
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Rabbit runs here ...");
        }
    }
}
