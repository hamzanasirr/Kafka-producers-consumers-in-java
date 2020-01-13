package com.github.hamzanasirr.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemoAssignSeek {
    private static final String BOOTSTRAP_SERVERS = Constants.BOOTSTRAP_SERVER_IP;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class);
    private static final String TOPIC = "third_topic";

    public static void main(String[] args) {
        // Setting the properties of the consumer
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Usually, it's 'latest'. But we want to see some data.

        // Create a consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        /* Assign and seek are mostly used to replay data or fetch a specific message */
        // Assign
        TopicPartition partition = new TopicPartition(TOPIC, 0);
        consumer.assign(Arrays.asList(partition));

        // Seek
        long offsetToReadFrom = 15L;
        consumer.seek(partition, offsetToReadFrom);

        int numberOfMessagesToread = 5;
        boolean keepOnReading = true;
        int numberOfMessagesReadSoFar = 0;

        // Poll for new data
        while (keepOnReading) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord record : records) {
                numberOfMessagesReadSoFar++;
                LOGGER.info("Key: " + record.key() + "\n" +
                            "Value: " + record.value() + "\n" +
                            "Partition: " + record.partition() + "\n" +
                            "Offset" + record.offset());
                if (numberOfMessagesReadSoFar >= numberOfMessagesToread) {
                    keepOnReading = false;
                }
            }
        }
    }
}
