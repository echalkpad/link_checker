package com.eogren.link_checker.scheduler;

import com.eogren.link_checker.messaging.producer.KafkaProducer;
import com.eogren.link_checker.scheduler.commands.Command;
import com.eogren.link_checker.scheduler.commands.ProduceScrapeRequestsCommand;
import com.eogren.link_checker.scheduler.commands.SendScrapeRequestCommand;
import com.eogren.link_checker.scheduler.config.KafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaProducerThread {
    protected static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaConfiguration config;
    private final BlockingQueue<Command> inputQueue;
    private final Thread workerThread;
    private final AtomicBoolean stop;

    private final KafkaProducer producer;

    public KafkaProducerThread(KafkaConfiguration config) {
        this.config = config;
        this.inputQueue = new ArrayBlockingQueue<>(100);
        this.workerThread = new Thread(this::workerFn, "KafkaProducerThread");
        this.stop = new AtomicBoolean(false);

        this.producer = new KafkaProducer(config);
    }


    public void start() {
        logger.info("Thread starting");
        this.workerThread.start();
    }

    public void stop() {
        stop.set(true);
        workerThread.interrupt();
        try {
            workerThread.join(2000);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while joining worker thread");
        }
    }

    public void workerFn() {
        while (!stop.get()) {
            try {
                Command c = inputQueue.take();
                if (c instanceof ProduceScrapeRequestsCommand) {
                    SendScrapeRequestCommand cmd = (SendScrapeRequestCommand) c;
                    producer.emitScrapeRequest(cmd.getUrl());
                } else {
                    logger.warn("Ignoring unknown message " + c.toString());
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public BlockingQueue<Command> getInputQueue() {
        return this.inputQueue;
    }
}
