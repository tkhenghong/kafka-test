package com.example.spring.boot.kafka.kafkatest.services.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Kafka Consumer Listener for testing whether the following logic works from:
 * https://github.com/contactsunny/SimpleKafkaExampleSpringBoot
 * TODO: Not using this due to lengthy implementation.
 */
public class KafkaConsumerListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KafkaConsumer<String, String> kafkaConsumer;

    public KafkaConsumerListener(List<String> topicNames, Properties consumerProperties) {
        kafkaConsumer = new KafkaConsumer<>(consumerProperties);
        kafkaConsumer.subscribe(topicNames);
    }

    public void startListening() {
        List<String> messages = new ArrayList<>();
        /**
         * As the Kafka Consumer is started, it will keep listening messages from Kafka.
         */
        while (true) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoDaysLater = LocalDateTime.now().plusDays(2);

            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.between(now, twoDaysLater));

            for (ConsumerRecord<String, String> record : records) {

                /*
                Whenever there's a new message in the Kafka topic, we'll get the message in this loop, as
                the record object.
                 */

                String message = record.value();

                logger.info("Received message: {}", message);

                messages.add(message);

                // NOTE: Auto acknowledge the messages.
            }


        }
    }

}
