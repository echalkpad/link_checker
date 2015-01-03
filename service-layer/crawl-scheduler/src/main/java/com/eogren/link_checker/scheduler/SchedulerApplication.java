package com.eogren.link_checker.scheduler;

import ch.qos.logback.classic.Level;
import com.eogren.link_checker.messaging.consumer.ScraperMessageKafkaConsumer;
import com.eogren.link_checker.messaging.producer.KafkaProducer;
import com.eogren.link_checker.scheduler.commands.Command;
import com.eogren.link_checker.scheduler.config.SchedulerApplicationConfig;
import com.eogren.link_checker.service_layer.client.ApiClient;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class SchedulerApplication {
    protected final Logger logger = LoggerFactory.getLogger(SchedulerApplication.class);
    public SchedulerApplication(String configPath) {
        SchedulerApplicationConfig config = createConfig(configPath);

        // Set up Kafka producer
        KafkaProducerThread producer = new KafkaProducerThread(config.getKafkaConfig());
        producer.start();

        // Set up main logic loop
        ApiClient apiClient = new ApiClient(config.getDataApiConfig().getDataApiHost());
        CommandExecutor mainLoop = new CommandExecutor(apiClient, config.getMonitoredPageInterval(), producer.getInputQueue());
        mainLoop.start();

        // Set up Kafka Consumer
        ScrapeUpdateProcessor processor = new ScrapeUpdateProcessor(mainLoop.getInputQueue(), apiClient);
        ScraperMessageKafkaConsumer consumer = new ScraperMessageKafkaConsumer(
                config.getKafkaConfig(), config.getKafkaConfig().getConsumerGroup(), processor
        );
        consumer.start(1);
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

    protected ApiClient createApiClient(SchedulerApplicationConfig config) {
        return new ApiClient(config.getDataApiConfig().getDataApiHost());
    }

    protected static SchedulerApplicationConfig createConfig(String configFile) {
        SchedulerApplicationConfig config;

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        try {
            config = mapper.readValue(new File(configFile), SchedulerApplicationConfig.class);
            Set<ConstraintViolation<SchedulerApplicationConfig>> violations = validator.validate(config);
            if (violations.isEmpty()) {
                return config;
            }

            throw new RuntimeException(buildConstraintMessages(violations));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing config!", e);
        }
    }

    protected static String buildConstraintMessages(Set<ConstraintViolation<SchedulerApplicationConfig>> violations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to parse config file:\n");
        for (ConstraintViolation<SchedulerApplicationConfig> violation : violations) {
            sb.append(String.format("%s %s\n", violation.getPropertyPath().toString(), violation.getMessage()));
        }

        return sb.toString();
    }
}
