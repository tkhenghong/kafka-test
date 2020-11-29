package com.example.spring.boot.kafka.kafkatest.controllers;

import com.example.spring.boot.kafka.kafkatest.models.KafkaAdvancedModel;
import com.example.spring.boot.kafka.kafkatest.services.kafka.KafkaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kafka/advanced")
public class KafkaAdvancedController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;

    private final KafkaService kafkaService;

    private final String kafkaTopic = "testing-advanced-topic";

    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public KafkaAdvancedController(ObjectMapper objectMapper, KafkaService kafkaService, @Qualifier("customKafkaTemplate") KafkaTemplate kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaService = kafkaService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/")
    public String sendKafkaMessage(@RequestBody KafkaAdvancedModel kafkaAdvancedModel) {
        String kafkaAdvancedModelString = null;
        try {
            kafkaAdvancedModelString = objectMapper.writeValueAsString(kafkaAdvancedModel);
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert KafkaAdvancedModel object from Object to JSON String.");
        }
        if (StringUtils.hasText(kafkaAdvancedModelString)) {
            // TODO: Move implementation of this method into kafkaService.addMessage()
            // kafkaService.addMessage(kafkaTopic, kafkaAdvancedModelString);

            kafkaTemplate.send(kafkaTopic, kafkaAdvancedModelString);
            logger.info("Message has been sent to {}.", kafkaTopic);
        }
        return "Message has been sent to " + kafkaTopic + ".";
    }

    @GetMapping("")
    public List<String> getKafkaTopicMessages() {
        List<String> messages = new ArrayList<>();
        logger.info("getKafkaTopicMessages()");
        // Create Kafka Consumer Factory.
        Map<String, Object> consumerConfigurations = kafkaService.generateConsumerConfigurations(null);
        ConsumerFactory<String, String> consumerFactory = kafkaService.generateConsumerFactory(consumerConfigurations);
        ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory =
                kafkaService.kafkaListenerContainerFactory(consumerFactory);

        // Create KafkaConsumer.
        ConsumerFactory consumerFactory1 = concurrentKafkaListenerContainerFactory.getConsumerFactory();
        Consumer consumer = consumerFactory1.createConsumer();
        List<String> kafkaTopics = new ArrayList<>();
        kafkaTopics.add(kafkaTopic);
        consumer.subscribe(kafkaTopics);

        // Polling Consumer records to 2 days.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = LocalDateTime.now().plusSeconds(1);
        ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.between(now, twoDaysLater));
        logger.info("consumerRecords.count(): {}", consumerRecords.count());

        // Getting messages.
        Iterable<ConsumerRecord<String, String>> consumerRecordIterable = consumerRecords.records(kafkaTopic);
        consumerRecordIterable.forEach(consumerRecord -> {
            String message = consumerRecord.value();
            logger.info("Extracted message: {}.", message);
            messages.add(message);

            consumer.commitSync(); // Acknowledge this message has been received.
        });

        consumer.close();

        // TODO: Move this implementation into kafkaService.getMessages().
//        return kafkaService.getMessages(kafkaTopic);
        return messages;
    }
}
