package com.eogren.link_checker.test_utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestUtils {
    public static void runWithTimeout(String cmdLine, int timeout, TimeUnit timeUnit) {
        try {
            Process p = Runtime.getRuntime().exec(cmdLine);
            boolean done = p.waitFor(timeout, timeUnit);
            if (!done) {
                throw new InterruptedException();
            }

            if (p.exitValue() != 0) {
                throw new RuntimeException(cmdLine + " failed with error code " + p.exitValue() + ". ");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(cmdLine + " failed to exit within 2 seconds -- is Kafka running?");
        } catch (IOException e) {
            throw new RuntimeException("Error running " + cmdLine + ": " + e.getMessage(), e);
        }
    }


    public static void checkFileExists(String name) {
        File f = new File(kafkaShell + "/" + name);
        if (!f.exists()) {
            throw new RuntimeException("Required file " + f.getAbsolutePath() + " does not exist");
        }

        if (!f.canExecute()) {
            throw new RuntimeException("Required file " + f.getAbsolutePath() + " must be executable");
        }
    }

    public static String getEnvWithDefault(String name, String defaultVal) {
        String ret = System.getenv(name);
        if (ret == null) {
            ret = defaultVal;
        }

        return ret;
    }
}
