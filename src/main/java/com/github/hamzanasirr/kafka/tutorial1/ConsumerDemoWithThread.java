package com.github.hamzanasirr.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThread {
    private static final String BOOTSTRAP_SERVERS = Constants.BOOTSTRAP_SERVER_IP;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDemoWithThread.class);
    private static final String GROUP_ID = "my-sixth-application";
    private static final String TOPIC = "third_topic";

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);

        // Starting the consumer thread.
        LOGGER.info("Creating the consumer thread!");
        Runnable consumerThread = new ConsumerThread(BOOTSTRAP_SERVERS, GROUP_ID, TOPIC, LOGGER, latch);
        Thread thread = new Thread(consumerThread);
        thread.start();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            LOGGER.info("Caught shutdown hook");
            ((ConsumerThread) consumerThread).shutDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } ));

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error("Application got interrupted", e);
        } finally {
            LOGGER.info("Application is closing");
        }
    }
}

@SuppressWarnings("FieldCanBeLocal")
class ConsumerThread implements Runnable {
    private CountDownLatch latch;
    private KafkaConsumer<String, String> consumer;
    private String bootstrapServers;
    private String topic;
    private String groupId;
    private Logger logger;

    public ConsumerThread(String bootstrapServers, String groupId, String topic, Logger logger, CountDownLatch latch) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topic = topic;
        this.logger = logger;
        this.latch = latch;

        // Create properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Usually, it's 'latest'. But we want to see some data.

        // Create consumer
        this.consumer = new KafkaConsumer<>(properties);
    }

    @Override
    public void run() {
        // Subscribe the consumer to topic(s)
        consumer.subscribe(Collections.singleton(topic));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord record : records) {
                    logger.info("Key: " + record.key() + "\n" +
                            "Value: " + record.value() + "\n" +
                            "Partition: " + record.partition() + "\n" +
                            "Offset" + record.offset());
                }
            }
        } catch (WakeupException e) {
            logger.info("Received shutdown signal!");
        } finally {
            consumer.close();

            // Tell the main code we are done with the consumer
            latch.countDown();
        }
    }

    public void shutDown() {
        consumer.wakeup(); // It is a special method to interrupt consumer.poll(). It will throw the WakeupException
    }
}