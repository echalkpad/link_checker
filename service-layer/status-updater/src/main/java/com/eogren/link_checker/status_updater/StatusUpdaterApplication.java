package com.eogren.link_checker.status_updater;

import ch.qos.logback.classic.Level;
import com.eogren.link_checker.status_updater.config.StatusUpdaterConfig;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class StatusUpdaterApplication {
    public static final Logger logger = LoggerFactory.getLogger(StatusUpdaterApplication.class);

    protected final StatusUpdaterConfig config;

    public StatusUpdaterApplication(String configFile) {
        config = createConfig(configFile);

        logger.info("I'm alive!!");
        logger.debug(String.format("ZK address: %s", config.getKafkaConfig().getZkAddress()));
        logger.debug(String.format("Type: %s", config.getKafkaConfig().getType()));
        logger.debug(String.format("Prefix: %s", config.getKafkaConfig().getPrefix()));
        logger.debug(String.format("Num threads: %d", config.getKafkaConfig().getNumThreads()));
    }

    protected static StatusUpdaterConfig createConfig(String configFile) {
        StatusUpdaterConfig config;

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        try {
            config = mapper.readValue(new File(configFile), StatusUpdaterConfig.class);
            Set<ConstraintViolation<StatusUpdaterConfig>> violations = validator.validate(config);
            if (violations.isEmpty()) {
                return config;
            }

            throw new RuntimeException(buildConstraintMessages(violations));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing config!", e);
        }
    }

    protected static String buildConstraintMessages(Set<ConstraintViolation<StatusUpdaterConfig>> violations) {
        StringBuffer sb = new StringBuffer();
        sb.append("Failed to parse config file:\n");
        for (ConstraintViolation<StatusUpdaterConfig> violation : violations) {
            sb.append(String.format("%s %s\n", violation.getPropertyPath().toString(), violation.getMessage()));
        }

        return sb.toString();
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

        StatusUpdaterApplication app = new StatusUpdaterApplication(args[args.length - 1]);
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
        return new java.io.File(StatusUpdaterApplication.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }
}