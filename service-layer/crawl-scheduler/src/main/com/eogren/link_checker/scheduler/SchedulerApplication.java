package com.eogren.link_checker.scheduler;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerApplication {
    public SchedulerApplication(String configPath) {
        System.out.println(configPath);
    }

    // Bootstrap Methods
    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
        }

        if (isVerbose(args)) {
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.eogren");
            logger.setLevel(Level.DEBUG);
        }

        new SchedulerApplication(args[args.length - 1]);
    }

    protected static boolean isVerbose(String[] args) {
        for (String s : args) {
            if (s.equals("-v")) {
                return true;
            }
        }

        return false;
    }

    protected static void usage() {
        System.out.println("Missing config file!");
        System.out.println(String.format("Correct usage: java -jar %s [-v] config_name", getJarName()));
        System.out.println("-v: Enable verbose logging");
        System.out.println("config_name: path to valid YAML config file");
        System.exit(1);
    }

    protected static String getJarName() {
        return new java.io.File(SchedulerApplication.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }
}
